#!/usr/bin/env python
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# This script translates Java to Objective-C using the j2objc transpiler.
#
# It assumes that you're running in Lucene's source root, not Lucene-Solr's
# top-level directory.
#
# The script is a work in progress. It does not translate non-Lucene source
# code as it has a lot of hard-coded dependency information. Nor does it
# translate Lucene tests since they are not yet supported.

import fnmatch
import os
import subprocess
import sys

# from translate_common import multiline_escaped_re
from translate_common import LUCENE_SRC_PATHS

# These two classes should really be put into their own respective .java
# files, but they currently live in DocFreqValueSource.java.
#
# Another class, FragmentQueue, also has the same problem. It lives in
# Highlighter.java (of package org.apache.lucene.search.highlight), but since
# it's only used by Highlighter, it's less of a problem.
FAULTY_INCLUDES = (
    '#include "org/apache/lucene/queries/function/valuesource/ConstDoubleDocValues.h"',  # nopep8
    '#include "org/apache/lucene/queries/function/valuesource/ConstIntDocValues.h"',  # nopep8
)

FAULTY_INCLUDE_FIX = '#include "org/apache/lucene/queries/function/valuesource/DocFreqValueSource.h"  // fixed by translate.py'  # nopep8


def postprocess_translated_objc(path):
    """
    Postprocess translated Objective-C code.
    """

    with open(path) as f:
        code = f.read()

    new_code = code

    for substr in FAULTY_INCLUDES:
        new_code = new_code.replace(substr, FAULTY_INCLUDE_FIX)

    # One-off fix for hunspell.
    new_code = new_code.replace(
        '#include "org/apache/lucene/analysis/hunspell/ISO8859_14Decoder.h"',
        '// disabled by translate.py'
    )
    new_code = new_code.replace(
        'return [new_OrgApacheLuceneAnalysisHunspellISO8859_14Decoder_init() autorelease];',  # nopep8
        '@throw [new_JavaLangRuntimeException_initWithNSString_(@"Not translated to Objective-C") autorelease];  // disabled by translate.py'  # nopep8
    )

    if new_code != code:
        with open(path, 'w') as f:
            f.write(new_code)


# excluded = ()

src_paths = LUCENE_SRC_PATHS

extra_cps = (
    # Should not be needed if we skip sandbox/queries/regex
    './sandbox/lib/jakarta-regexp-1.4.jar',

    # fix ConstDoubleDocValues
    './build/queries/classes/java'
)


excluded = (
    # No need to translate j2objc annotations to Objective-C.
    './core/src/java/org/lukhnos/portmobile/j2objc/*',

    # Currently skipped; these currently relies on JDK 7's BreakIterator,
    # which, although supported on Android, is not implemented by j2objc's
    # jre_emul. See the discussion here for porting considerations:
    # https://groups.google.com/forum/#!topic/j2objc-discuss/Rx7ioYfOaIU
    './analysis/common/src/java/org/apache/lucene/analysis/hunspell/ISO8859_14Decoder.java',  # nopep8
    './analysis/common/src/java/org/apache/lucene/analysis/th/*.java',
    './analysis/common/src/java/org/apache/lucene/analysis/util/CharArrayIterator.java',  # nopep8
    './analysis/common/src/java/org/apache/lucene/analysis/util/SegmentingTokenizerBase.java',  # nopep8

    # Requires antlr.
    './expressions/src/java/org/apache/lucene/expressions/js/*.java',

    # Requires BreakIterator.
    './highlighter/src/java/org/apache/lucene/search/postingshighlight/*.java',  # nopep8
    './highlighter/src/java/org/apache/lucene/search/vectorhighlight/*.java',  # nopep8

    # Uses native methods.
    './misc/src/java/org/apache/lucene/store/*.java',
    # './misc/src/java/org/apache/lucene/uninverting/*.java',

    # './misc/src/java/org/apache/lucene/misc/store/*.java',
    # './misc/src/java/org/apache/lucene/misc/uninverting/*.java',

    # Currently skipped; it relies on ./sandbox/lib/jakarta-regexp-1.4.jar
    # and we don't have that library translated to Objective-C yet.
    './sandbox/src/java/org/apache/lucene/sandbox/queries/regex/*.java'
    # './sandbox/src/java/org/apache/lucene/queries/regex/*.java',
)

cps = src_paths + extra_cps
classpaths = ':'.join(cps)
print('classpaths: %s' % classpaths)

dst = './build/objc'

if not os.path.exists(dst):
    os.mkdir(dst)
    print('%s created' % dst)

for src in src_paths:
    to_compile = []
    to_postprocess = []

    for base, dirs, files in os.walk(src):
        for file_path in files:
            if not fnmatch.fnmatch(file_path, "*.java"):
                continue

            full_path_java = os.path.join(base, file_path)
            if any(fnmatch.fnmatch(full_path_java, ptn) for ptn in excluded):
                continue

            full_path_m = full_path_java.replace(
                src, dst).replace(".java", ".m")
            if os.path.exists(full_path_m):
                if os.path.getmtime(full_path_m) >= os.path.getmtime(full_path_java):  # nopep8
                    continue
            to_compile.append(full_path_java)
            to_postprocess.append(full_path_m)

    print('%s => %s files to compile' % (src, len(to_compile)))

    if to_compile:
        print('Compiling %d java files' % len(to_compile))
        args = [
            'j2objc',
            # '-use-arc',
            '-classpath', classpaths,
            '--segmented-headers',
            '-sourcepath', src,
            '-d', dst,
        ]
        args.extend(sys.argv[1:])
        args.extend(to_compile)
        subprocess.call(args)
        # TODO: Check error code

    # TODO: Only transform successful code
    for path in to_postprocess:
        print('postprocessing: %s' % path)
        postprocess_translated_objc(path)
