#!/usr/bin/env python
import fnmatch
import os
import subprocess
import sys

from translate_common import LUCENE_SRC_PATHS

# excluded = ()

src_paths = LUCENE_SRC_PATHS

extra_cps = (
    './sandbox/lib/jakarta-regexp-1.4.jar',
    './build/queries/classes/java'
)

excluded = (
    './analysis/common/src/java/org/apache/lucene/analysis/hunspell/ISO8859_14Decoder.java',
    './analysis/common/src/java/org/apache/lucene/analysis/th/*.java',
    './analysis/common/src/java/org/apache/lucene/analysis/util/CharArrayIterator.java',
    './analysis/common/src/java/org/apache/lucene/analysis/util/SegmentingTokenizerBase.java',
    './expressions/src/java/org/apache/lucene/expressions/js/*.java',
    './highlighter/src/java/org/apache/lucene/search/postingshighlight/*.java',
    './highlighter/src/java/org/apache/lucene/search/vectorhighlight/*.java',
    './join/src/java/org/apache/lucene/search/join/ToParentBlockJoinCollector.java',
    # './join/src/java/org/apache/lucene/search/join/ToParentBlockJoinQuery.java'
)

cps = src_paths + extra_cps
classpaths = ':'.join(cps)
print('%s' % classpaths)
# sys.exit(1)

dst = './build/objc'

if not os.path.exists(dst):
    os.mkdir(dst)
    print('%s created' % dst)

for src in src_paths:
    to_compile = []

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

"""
# TODO: Argparse
# TODO: Support Gradle layout (src/main/java)

TRANSLATE_TEST = False

src = './src/java'
dst = './src/objc'

if TRANSLATE_TEST:
    src = './src/test'
    dst = './src/test-objc'

if not os.path.exists(src):
    print('Script must be run in the parent directory of src')
    sys.exit(1)


# TODO: Automatically collect package names and provide shortening.


"""
