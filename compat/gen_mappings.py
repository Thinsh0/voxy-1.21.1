"""
Generates compat/mappings-1.21.1.tiny from compat/renames.txt.

The 1.21.1 port compiles the shared upstream sources against 1.21.1 with Mojang names,
plus this extra mapping layer which applies the renames Mojang did in later versions
(e.g. ResourceLocation -> Identifier). This keeps the java sources identical to upstream.

Mojang mappings name a method in every class that overrides it, so a method rename must be
applied to every class that redeclares it, which is why this file is generated rather than hand written.

Usage: python compat/gen_mappings.py   (after a first gradle build so the loom cache is populated)
"""
import glob
import os
import re

HERE = os.path.dirname(os.path.abspath(__file__))
LOOM = os.path.join(os.path.expanduser("~"), ".gradle", "caches", "fabric-loom", "1.21.1")
PRIMITIVES = {"V": "void", "Z": "boolean", "B": "byte", "C": "char", "S": "short", "I": "int", "J": "long", "F": "float", "D": "double"}


def main():
    class_renames = {}  # intermediary -> new name
    method_renames = {}  # intermediary -> (old mojang name, new name)
    field_renames = {}  # intermediary -> new name
    with open(os.path.join(HERE, "renames.txt")) as f:
        for line in f:
            p = line.split("#", 1)[0].split()
            if not p:
                continue
            if p[0] == "class":
                class_renames[p[1]] = p[2]
            elif p[0] == "method":
                method_renames[p[1]] = (p[2], p[3])
            elif p[0] == "field":
                field_renames[p[1]] = p[3]
            else:
                raise ValueError(line)

    # official -> intermediary, the official name/desc of every renamed method, and renamed fields
    off2int = {}
    roots = {}  # intermediary method -> (official name, official desc)
    fields = {}  # obf class -> [(official desc, official name, intermediary)]
    with open(os.path.join(LOOM, "intermediary-v2.tiny")) as f:
        assert f.readline().split()[3:5] == ["official", "intermediary"]
        cur_cls = None
        for line in f:
            p = line.rstrip("\n").split("\t")
            if p[0] == "c":
                off2int[p[1]] = p[2]
                cur_cls = p[1]
            elif len(p) > 4 and p[1] == "m" and p[4] in method_renames:
                assert p[2].startswith("()"), "only no-arg methods are supported"
                roots[p[4]] = (p[3], p[2])
            elif len(p) > 4 and p[1] == "f" and p[4] in field_renames:
                fields.setdefault(cur_cls, []).append((p[2], p[3], p[4]))
    assert set(roots) == set(method_renames), f"methods not found: {set(method_renames) - set(roots)}"
    assert {f[2] for fs in fields.values() for f in fs} == set(field_renames), "fields not found"
    int2off = {i: o for o, i in off2int.items()}

    mojang_txt = sorted(glob.glob(os.path.join(LOOM, "layered", "working_dir", "*", "mojang", "*.txt")))
    assert mojang_txt, "run a gradle build first so loom downloads the mojang mappings"
    entries = []  # (obf class, mojang class, return type, mojang name, obf name)
    obf2named = {}  # both in internal (slash) form
    for path in mojang_txt:
        with open(path, encoding="utf-8") as f:
            cur = None
            for line in f:
                if line.startswith("#"):
                    continue
                if not line.startswith(" "):
                    named, obf = line.rstrip(":\n").replace(".", "/").split(" -> ")
                    cur = obf
                    obf2named[obf] = named
                    continue
                m = re.match(r"\s+(?:\d+:\d+:)?(\S+) (\w+)\(\)(?::\d+:\d+)? -> (\S+)$", line.rstrip("\n"))
                if m:
                    entries.append((cur, m.group(1), m.group(2), m.group(3)))

    def java_type(desc_ret):
        if desc_ret in PRIMITIVES:
            return PRIMITIVES[desc_ret]
        return obf2named.get(desc_ret[1:-1], desc_ret[1:-1]).replace("/", ".")

    wanted = {}  # (old name, obf name, return type) -> intermediary
    for inter, (obf_name, desc) in roots.items():
        wanted[(method_renames[inter][0], obf_name, java_type(desc[2:]))] = inter

    per_class = {}  # obf class -> set of renamed intermediary methods
    for obf_cls, ret, name, obf_name in entries:
        inter = wanted.get((name, obf_name, ret))
        if inter is not None and obf_cls in off2int:
            per_class.setdefault(obf_cls, set()).add(inter)
    for obf_cls in fields:
        per_class.setdefault(obf_cls, set())

    # class renames also move their inner classes, keeping the mojang name of the inner part (e.g. GlDebug$LogEntry)
    renamed = {}  # obf class -> new name
    for src in class_renames:
        if src not in int2off and src in obf2named:  # unobfuscated classes without members are not in intermediary
            off2int[src] = int2off[src] = src
    for obf_cls, inter_cls in off2int.items():
        outer = inter_cls.split("$", 1)[0]
        if outer in class_renames:
            renamed[obf_cls] = class_renames[outer] + obf2named[obf_cls][len(obf2named[int2off[outer]]):]
            per_class.setdefault(obf_cls, set())
    assert {off2int[o].split("$", 1)[0] for o in renamed} == set(class_renames), "classes not found"

    # Keyed on the official namespace: when merging on intermediary, loom gives the mojang override
    # entries (which have no intermediary name) fallback names, which then conflict when remapping
    out = ["tiny\t2\t0\tofficial\tnamed"]
    for obf_cls in sorted(per_class, key=lambda o: off2int[o]):
        out.append(f"c\t{obf_cls}\t{renamed.get(obf_cls, obf2named[obf_cls])}")
        for inter in sorted(per_class[obf_cls]):
            out.append(f"\tm\t{roots[inter][1]}\t{roots[inter][0]}\t{method_renames[inter][1]}")
        for desc, name, inter in fields.get(obf_cls, []):
            out.append(f"\tf\t{desc}\t{name}\t{field_renames[inter]}")

    with open(os.path.join(HERE, "mappings-1.21.1.tiny"), "w", newline="\n") as f:
        f.write("\n".join(out) + "\n")
    print(f"wrote {len(out) - 1} lines")


if __name__ == "__main__":
    main()
