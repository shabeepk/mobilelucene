#!/usr/bin/env python
import fnmatch
import os
import re
import sys

from translate_common import LUCENE_SRC_PATHS


import_map = {
    'java.nio.file.': 'org.lukhnos.portmobile.file.',
    'java.nio.file.attribute.': 'org.lukhnos.portmobile.file.attribute.',
    'java.lang.invoke.': 'org.lukhnos.portmobile.invoke.',
    'java.util.Objects': 'org.lukhnos.portmobile.util.Objects',
    'java.nio.charset.StandardCharsets': 'org.lukhnos.portmobile.charset.StandardCharsets',  # nopep8
}

import_map_re = {
    re.compile(re.escape(k)): v for k, v in import_map.items()
}

extra_imports = {
    'FileChannel.open': 'org.lukhnos.portmobile.channels.utils.FileChannelUtils',  # nopep8
    'new ClassValue': 'org.lukhnos.portmobile.lang.ClassValue',
    '// j2objc:"Weak"': 'org.lukhnos.portmobile.j2objc.annotations.Weak',
    '// j2objc:"WeakOuter"': 'org.lukhnos.portmobile.j2objc.annotations.WeakOuter',  # nopep8
}

extra_imports_re = {
    re.compile(re.escape(k), re.M | re.S): v for k, v in extra_imports.items()
}

method_calls = {
    'FileChannel.open': 'FileChannelUtils.open',
    '// j2objc:"Weak"': '@Weak',
    '// j2objc:"WeakOuter"': '@WeakOuter',
}

method_calls_re = {
    re.compile(re.escape(k), re.M | re.S): v for k, v in method_calls.items()
}

comments = {
    '{@link Files#newByteChannel(Path, java.nio.file.OpenOption...)}.':
        '{@link Files#newByteChannel(Path, org.lukhnos.portmobile.file.StandardOpenOption)}'  # nopep8
}

comments_re = {
    re.compile(re.escape(k), re.M | re.S): v for k, v in comments.items()
}

other_re = {
    re.compile(r'ReflectiveOperationException(\s*\|\s*\w*?Exception)?', re.M | re.S): 'Exception',  # nopep8
}

extra_import_tagline = '// Extra imports by portmobile.'


CODE_BLOCK_RE = re.compile(r'(.+import\s+.+?\n)(.+)', re.M | re.S)


def process_source(path):
    with open(path) as f:
        code = f.read()

    m = CODE_BLOCK_RE.match(code)
    if not m:
        return

    head_lines = m.group(1).split('\n')
    body = m.group(2)

    new_head_lines = []
    for line in head_lines:
        text = line
        for p, r in import_map_re.items():
            text = p.sub(r, text)
        new_head_lines.append(text)

    extras = []
    for p, i in extra_imports_re.items():
        if p.search(body):
            extras.append('import %s;' % i)

    # TODO: Make this idempotent.
    if extras and not extra_import_tagline in head_lines:
        new_head_lines.append(extra_import_tagline)
        new_head_lines.extend(extras)
        new_head_lines.append('')

    new_body = body
    for p, r in method_calls_re.items():
        new_body = p.sub(r, new_body)

    for p, r in comments_re.items():
        new_body = p.sub(r, new_body)

    for p, r in other_re.items():
        new_body = p.sub(r, new_body)

    new_code = '\n'.join(new_head_lines) + new_body

    if head_lines != new_head_lines or body != new_body:
        with open(path, 'w') as f:
            f.write(new_code)


if len(sys.argv) > 1:
    src_paths = sys.argv[1]
else:
    src_paths = LUCENE_SRC_PATHS

print('using path: %s' % ':'.join(src_paths))

for src_path in src_paths:
    for base, dirs, files in os.walk(src_path):
        for file_path in files:
            if not fnmatch.fnmatch(file_path, "*.java"):
                continue
            full_path_java = os.path.join(base, file_path)
            # print(full_path_java)
            process_source(full_path_java)
