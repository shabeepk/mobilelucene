package org.apache.lucene.util;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;

/**
 * Helper class for loading SPI classes from classpath (META-INF files).
 * This is a light impl of {@link java.util.ServiceLoader} but is guaranteed to
 * be bug-free regarding classpath order and does not instantiate or initialize
 * the classes found.
 *
 * @lucene.internal
 */
public final class SPIClassIterator<S> implements Iterator<Class<? extends S>> {
  private static final Map<String, List<String>> hardCodedServices;
  static {
    hardCodedServices = new HashMap<String, List<String>>();
    List<String> list;

    /*

    list = new ArrayList<String>();
    hardCodedServices.put("org.apache.lucene.analysis.util.CharFilterFactory", list);
    list.add("org.apache.lucene.analysis.charfilter.HTMLStripCharFilterFactory");
    list.add("org.apache.lucene.analysis.charfilter.MappingCharFilterFactory");
    list.add("org.apache.lucene.analysis.fa.PersianCharFilterFactory");
    list.add("org.apache.lucene.analysis.pattern.PatternReplaceCharFilterFactory");

    list = new ArrayList<String>();
    hardCodedServices.put("org.apache.lucene.analysis.util.TokenFilterFactory", list);
    list.add("org.apache.lucene.analysis.tr.ApostropheFilterFactory");
    list.add("org.apache.lucene.analysis.ar.ArabicNormalizationFilterFactory");
    list.add("org.apache.lucene.analysis.ar.ArabicStemFilterFactory");
    list.add("org.apache.lucene.analysis.bg.BulgarianStemFilterFactory");
    list.add("org.apache.lucene.analysis.br.BrazilianStemFilterFactory");
    list.add("org.apache.lucene.analysis.cjk.CJKBigramFilterFactory");
    list.add("org.apache.lucene.analysis.cjk.CJKWidthFilterFactory");
    list.add("org.apache.lucene.analysis.ckb.SoraniNormalizationFilterFactory");
    list.add("org.apache.lucene.analysis.ckb.SoraniStemFilterFactory");
    list.add("org.apache.lucene.analysis.commongrams.CommonGramsFilterFactory");
    list.add("org.apache.lucene.analysis.commongrams.CommonGramsQueryFilterFactory");
    list.add("org.apache.lucene.analysis.compound.DictionaryCompoundWordTokenFilterFactory");
    list.add("org.apache.lucene.analysis.compound.HyphenationCompoundWordTokenFilterFactory");
    list.add("org.apache.lucene.analysis.core.LowerCaseFilterFactory");
    list.add("org.apache.lucene.analysis.core.StopFilterFactory");
    list.add("org.apache.lucene.analysis.core.TypeTokenFilterFactory");
    list.add("org.apache.lucene.analysis.core.UpperCaseFilterFactory");
    list.add("org.apache.lucene.analysis.cz.CzechStemFilterFactory");
    list.add("org.apache.lucene.analysis.de.GermanLightStemFilterFactory");
    list.add("org.apache.lucene.analysis.de.GermanMinimalStemFilterFactory");
    list.add("org.apache.lucene.analysis.de.GermanNormalizationFilterFactory");
    list.add("org.apache.lucene.analysis.de.GermanStemFilterFactory");
    list.add("org.apache.lucene.analysis.el.GreekLowerCaseFilterFactory");
    list.add("org.apache.lucene.analysis.el.GreekStemFilterFactory");
    list.add("org.apache.lucene.analysis.en.EnglishMinimalStemFilterFactory");
    list.add("org.apache.lucene.analysis.en.EnglishPossessiveFilterFactory");
    list.add("org.apache.lucene.analysis.en.KStemFilterFactory");
    list.add("org.apache.lucene.analysis.en.PorterStemFilterFactory");
    list.add("org.apache.lucene.analysis.es.SpanishLightStemFilterFactory");
    list.add("org.apache.lucene.analysis.fa.PersianNormalizationFilterFactory");
    list.add("org.apache.lucene.analysis.fi.FinnishLightStemFilterFactory");
    list.add("org.apache.lucene.analysis.fr.FrenchLightStemFilterFactory");
    list.add("org.apache.lucene.analysis.fr.FrenchMinimalStemFilterFactory");
    list.add("org.apache.lucene.analysis.ga.IrishLowerCaseFilterFactory");
    list.add("org.apache.lucene.analysis.gl.GalicianMinimalStemFilterFactory");
    list.add("org.apache.lucene.analysis.gl.GalicianStemFilterFactory");
    list.add("org.apache.lucene.analysis.hi.HindiNormalizationFilterFactory");
    list.add("org.apache.lucene.analysis.hi.HindiStemFilterFactory");
    list.add("org.apache.lucene.analysis.hu.HungarianLightStemFilterFactory");
    list.add("org.apache.lucene.analysis.hunspell.HunspellStemFilterFactory");
    list.add("org.apache.lucene.analysis.id.IndonesianStemFilterFactory");
    list.add("org.apache.lucene.analysis.in.IndicNormalizationFilterFactory");
    list.add("org.apache.lucene.analysis.it.ItalianLightStemFilterFactory");
    list.add("org.apache.lucene.analysis.lv.LatvianStemFilterFactory");
    list.add("org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory");
    list.add("org.apache.lucene.analysis.miscellaneous.CapitalizationFilterFactory");
    list.add("org.apache.lucene.analysis.miscellaneous.CodepointCountFilterFactory");
    list.add("org.apache.lucene.analysis.miscellaneous.HyphenatedWordsFilterFactory");
    list.add("org.apache.lucene.analysis.miscellaneous.KeepWordFilterFactory");
    list.add("org.apache.lucene.analysis.miscellaneous.KeywordMarkerFilterFactory");
    list.add("org.apache.lucene.analysis.miscellaneous.KeywordRepeatFilterFactory");
    list.add("org.apache.lucene.analysis.miscellaneous.LengthFilterFactory");
    list.add("org.apache.lucene.analysis.miscellaneous.LimitTokenCountFilterFactory");
    list.add("org.apache.lucene.analysis.miscellaneous.LimitTokenOffsetFilterFactory");
    list.add("org.apache.lucene.analysis.miscellaneous.LimitTokenPositionFilterFactory");
    list.add("org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilterFactory");
    list.add("org.apache.lucene.analysis.miscellaneous.StemmerOverrideFilterFactory");
    list.add("org.apache.lucene.analysis.miscellaneous.TrimFilterFactory");
    list.add("org.apache.lucene.analysis.miscellaneous.TruncateTokenFilterFactory");
    list.add("org.apache.lucene.analysis.miscellaneous.WordDelimiterFilterFactory");
    list.add("org.apache.lucene.analysis.miscellaneous.ScandinavianFoldingFilterFactory");
    list.add("org.apache.lucene.analysis.miscellaneous.ScandinavianNormalizationFilterFactory");
    list.add("org.apache.lucene.analysis.ngram.EdgeNGramFilterFactory");
    list.add("org.apache.lucene.analysis.ngram.NGramFilterFactory");
    list.add("org.apache.lucene.analysis.no.NorwegianLightStemFilterFactory");
    list.add("org.apache.lucene.analysis.no.NorwegianMinimalStemFilterFactory");
    list.add("org.apache.lucene.analysis.pattern.PatternReplaceFilterFactory");
    list.add("org.apache.lucene.analysis.pattern.PatternCaptureGroupFilterFactory");
    list.add("org.apache.lucene.analysis.payloads.DelimitedPayloadTokenFilterFactory");
    list.add("org.apache.lucene.analysis.payloads.NumericPayloadTokenFilterFactory");
    list.add("org.apache.lucene.analysis.payloads.TokenOffsetPayloadTokenFilterFactory");
    list.add("org.apache.lucene.analysis.payloads.TypeAsPayloadTokenFilterFactory");
    list.add("org.apache.lucene.analysis.pt.PortugueseLightStemFilterFactory");
    list.add("org.apache.lucene.analysis.pt.PortugueseMinimalStemFilterFactory");
    list.add("org.apache.lucene.analysis.pt.PortugueseStemFilterFactory");
    list.add("org.apache.lucene.analysis.reverse.ReverseStringFilterFactory");
    list.add("org.apache.lucene.analysis.ru.RussianLightStemFilterFactory");
    list.add("org.apache.lucene.analysis.shingle.ShingleFilterFactory");
    list.add("org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory");
    list.add("org.apache.lucene.analysis.sr.SerbianNormalizationFilterFactory");
    list.add("org.apache.lucene.analysis.standard.ClassicFilterFactory");
    list.add("org.apache.lucene.analysis.standard.StandardFilterFactory");
    list.add("org.apache.lucene.analysis.sv.SwedishLightStemFilterFactory");
    list.add("org.apache.lucene.analysis.synonym.SynonymFilterFactory");
    list.add("org.apache.lucene.analysis.th.ThaiWordFilterFactory");
    list.add("org.apache.lucene.analysis.tr.TurkishLowerCaseFilterFactory");
    list.add("org.apache.lucene.analysis.util.ElisionFilterFactory");

    list = new ArrayList<String>();
    hardCodedServices.put("org.apache.lucene.analysis.util.TokenizerFactory", list);
    list.add("org.apache.lucene.analysis.core.KeywordTokenizerFactory");
    list.add("org.apache.lucene.analysis.core.LetterTokenizerFactory");
    list.add("org.apache.lucene.analysis.core.LowerCaseTokenizerFactory");
    list.add("org.apache.lucene.analysis.core.WhitespaceTokenizerFactory");
    list.add("org.apache.lucene.analysis.ngram.EdgeNGramTokenizerFactory");
    list.add("org.apache.lucene.analysis.ngram.NGramTokenizerFactory");
    list.add("org.apache.lucene.analysis.path.PathHierarchyTokenizerFactory");
    list.add("org.apache.lucene.analysis.pattern.PatternTokenizerFactory");
    list.add("org.apache.lucene.analysis.standard.ClassicTokenizerFactory");
    list.add("org.apache.lucene.analysis.standard.StandardTokenizerFactory");
    list.add("org.apache.lucene.analysis.standard.UAX29URLEmailTokenizerFactory");
    list.add("org.apache.lucene.analysis.th.ThaiTokenizerFactory");
    list.add("org.apache.lucene.analysis.wikipedia.WikipediaTokenizerFactory");

    list = new ArrayList<String>();
    hardCodedServices.put("org.apache.lucene.codecs.Codec", list);
    list.add("org.apache.lucene.codecs.simpletext.SimpleTextCodec");

    list = new ArrayList<String>();
    hardCodedServices.put("org.apache.lucene.codecs.DocValuesFormat", list);
    list.add("org.apache.lucene.codecs.memory.MemoryDocValuesFormat");
    list.add("org.apache.lucene.codecs.memory.DirectDocValuesFormat");
    list.add("org.apache.lucene.codecs.simpletext.SimpleTextDocValuesFormat");

    list = new ArrayList<String>();
    hardCodedServices.put("org.apache.lucene.codecs.PostingsFormat", list);
    list.add("org.apache.lucene.codecs.blocktreeords.BlockTreeOrdsPostingsFormat");
    list.add("org.apache.lucene.codecs.bloom.BloomFilteringPostingsFormat");
    list.add("org.apache.lucene.codecs.memory.DirectPostingsFormat");
    list.add("org.apache.lucene.codecs.memory.FSTOrdPostingsFormat");
    list.add("org.apache.lucene.codecs.memory.FSTPostingsFormat");
    list.add("org.apache.lucene.codecs.memory.MemoryPostingsFormat");
    list.add("org.apache.lucene.codecs.simpletext.SimpleTextPostingsFormat");
    list.add("org.apache.lucene.codecs.autoprefix.AutoPrefixPostingsFormat");
    */
    list = new ArrayList<String>();
    hardCodedServices.put("org.apache.lucene.codecs.Codec", list);
    list.add("org.apache.lucene.codecs.lucene53.Lucene53Codec");

    list = new ArrayList<String>();
    hardCodedServices.put("org.apache.lucene.codecs.DocValuesFormat", list);
    list.add("org.apache.lucene.codecs.lucene50.Lucene50DocValuesFormat");

    list = new ArrayList<String>();
    hardCodedServices.put("org.apache.lucene.codecs.PostingsFormat", list);
    list.add("org.apache.lucene.codecs.lucene50.Lucene50PostingsFormat");
  }

  private final Class<S> clazz;
  private final ClassLoader loader;
  private boolean loaded;
  private Iterator<String> linesIterator;
  
  public static <S> SPIClassIterator<S> get(Class<S> clazz) {
    return new SPIClassIterator<>(clazz, Thread.currentThread().getContextClassLoader());
  }
  
  public static <S> SPIClassIterator<S> get(Class<S> clazz, ClassLoader loader) {
    return new SPIClassIterator<>(clazz, loader);
  }
  
  /** Utility method to check if some class loader is a (grand-)parent of or the same as another one.
   * This means the child will be able to load all classes from the parent, too. */
  public static boolean isParentClassLoader(final ClassLoader parent, ClassLoader child) {
    while (child != null) {
      if (child == parent) {
        return true;
      }
      child = child.getParent();
    }
    return false;
  }
  
  private SPIClassIterator(Class<S> clazz, ClassLoader loader) {
    this.clazz = clazz;

    List<String> classList = hardCodedServices.get(clazz.getName());
    if (classList == null || classList.isEmpty()) {
      throw new ServiceConfigurationError("Error loading SPI profiles for type " + clazz.getName() + " from hard-coded services");
    }

    this.loader = (loader == null) ? ClassLoader.getSystemClassLoader() : loader;
    this.linesIterator = Collections.<String>emptySet().iterator();
    this.loaded = false;
  }
  
  private boolean loadNextProfile() {
    if (loaded) {
      return false;
    }

    loaded = true;
    linesIterator = hardCodedServices.get(this.clazz.getName()).iterator();
    return true;
  }
  
  @Override
  public boolean hasNext() {
    return linesIterator.hasNext() || loadNextProfile();
  }
  
  @Override
  public Class<? extends S> next() {
    // hasNext() implicitely loads the next profile, so it is essential to call this here!
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    assert linesIterator.hasNext();
    final String c = linesIterator.next();
    try {
      // don't initialize the class (pass false as 2nd parameter):
      return Class.forName(c, false, loader).asSubclass(clazz);
    } catch (ClassNotFoundException cnfe) {
      throw new ServiceConfigurationError(String.format(Locale.ROOT, "An SPI class of type %s with classname %s does not exist.", clazz.getName(), c));
    }
  }
  
  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
  
}
