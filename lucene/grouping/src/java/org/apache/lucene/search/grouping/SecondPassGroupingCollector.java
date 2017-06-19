/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.search.grouping;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.lukhnos.portmobile.util.Objects;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopScoreDocCollector;

/**
 * SecondPassGroupingCollector is the second of two passes
 * necessary to collect grouped docs.  This pass gathers the
 * top N documents per top group computed from the
 * first pass. Concrete subclasses define what a group is and how it
 * is internally collected.
 *
 * <p>See {@link org.apache.lucene.search.grouping} for more
 * details including a full code example.</p>
 *
 * @lucene.experimental
 */
public abstract class SecondPassGroupingCollector<T> extends SimpleCollector {

  private final Collection<SearchGroup<T>> groups;
  private final Sort groupSort;
  private final Sort withinGroupSort;
  private final int maxDocsPerGroup;
  private final boolean needsScores;
  protected final Map<T, SearchGroupDocs<T>> groupMap;

  protected SearchGroupDocs<T>[] groupDocs;

  private int totalHitCount;
  private int totalGroupedHitCount;

  public SecondPassGroupingCollector(Collection<SearchGroup<T>> groups, Sort groupSort, Sort withinGroupSort,
                                     int maxDocsPerGroup, boolean getScores, boolean getMaxScores, boolean fillSortFields)
    throws IOException {

    //System.out.println("SP init");
    if (groups.isEmpty()) {
      throw new IllegalArgumentException("no groups to collect (groups is empty)");
    }

    this.groups = Objects.requireNonNull(groups);
    this.groupSort = Objects.requireNonNull(groupSort);
    this.withinGroupSort = Objects.requireNonNull(withinGroupSort);
    this.maxDocsPerGroup = maxDocsPerGroup;
    this.needsScores = getScores || getMaxScores || withinGroupSort.needsScores();

    this.groupMap = new HashMap<>(groups.size());
    for (SearchGroup<T> group : groups) {
      //System.out.println("  prep group=" + (group.groupValue == null ? "null" : group.groupValue.utf8ToString()));
      final TopDocsCollector<?> collector;
      if (withinGroupSort.equals(Sort.RELEVANCE)) { // optimize to use TopScoreDocCollector
        // Sort by score
        collector = TopScoreDocCollector.create(maxDocsPerGroup);
      } else {
        // Sort by fields
        collector = TopFieldCollector.create(withinGroupSort, maxDocsPerGroup, fillSortFields, getScores, getMaxScores);
      }
      groupMap.put(group.groupValue, new SearchGroupDocs<>(group.groupValue, collector));
    }
  }

  @Override
  public boolean needsScores() {
    return needsScores;
  }

  @Override
  public void setScorer(Scorer scorer) throws IOException {
    for (SearchGroupDocs<T> group : groupMap.values()) {
      group.leafCollector.setScorer(scorer);
    }
  }

  @Override
  public void collect(int doc) throws IOException {
    totalHitCount++;
    SearchGroupDocs<T> group = retrieveGroup(doc);
    if (group != null) {
      totalGroupedHitCount++;
      group.leafCollector.collect(doc);
    }
  }

  /**
   * Returns the group the specified doc belongs to or <code>null</code> if no group could be retrieved.
   *
   * @param doc The specified doc
   * @return the group the specified doc belongs to or <code>null</code> if no group could be retrieved
   * @throws IOException If an I/O related error occurred
   */
  protected abstract SearchGroupDocs<T> retrieveGroup(int doc) throws IOException;

  @Override
  protected void doSetNextReader(LeafReaderContext readerContext) throws IOException {
    //System.out.println("SP.setNextReader");
    for (SearchGroupDocs<T> group : groupMap.values()) {
      group.leafCollector = group.collector.getLeafCollector(readerContext);
    }
  }

  public TopGroups<T> getTopGroups(int withinGroupOffset) {
    @SuppressWarnings({"unchecked","rawtypes"})
    final GroupDocs<T>[] groupDocsResult = (GroupDocs<T>[]) new GroupDocs[groups.size()];

    int groupIDX = 0;
    float maxScore = Float.MIN_VALUE;
    for(SearchGroup<?> group : groups) {
      final SearchGroupDocs<T> groupDocs = groupMap.get(group.groupValue);
      final TopDocs topDocs = groupDocs.collector.topDocs(withinGroupOffset, maxDocsPerGroup);
      groupDocsResult[groupIDX++] = new GroupDocs<>(Float.NaN,
                                                                    topDocs.getMaxScore(),
                                                                    topDocs.totalHits,
                                                                    topDocs.scoreDocs,
                                                                    groupDocs.groupValue,
                                                                    group.sortValues);
      maxScore = Math.max(maxScore, topDocs.getMaxScore());
    }

    return new TopGroups<>(groupSort.getSort(),
                                           withinGroupSort.getSort(),
                                           totalHitCount, totalGroupedHitCount, groupDocsResult,
                                           maxScore);
  }


  // TODO: merge with SearchGroup or not?
  // ad: don't need to build a new hashmap
  // disad: blows up the size of SearchGroup if we need many of them, and couples implementations
  public class SearchGroupDocs<T> {

    public final T groupValue;
    public final TopDocsCollector<?> collector;
    public LeafCollector leafCollector;

    public SearchGroupDocs(T groupValue, TopDocsCollector<?> collector) {
      this.groupValue = groupValue;
      this.collector = collector;
    }
  }
}
