import re
import json
import os
import traceback

def convert_md_to_json(textfile):
    text = textfile.read()

    postid = textfile.name.split(".")[0].split("/")[-1]

    text = static_fixes(text, postid)

    r_title = re.compile(r"\s*\*\*(\d*[dD]\d+)\s+(.+)\*\*")
    r_category = re.compile(r"\*\*(?!\d*[dD]\d+)(.+)\*\*")
    r_item = re.compile(r"(\d+-?\d*)\.\s*(.+)")

    r_link = re.compile(r"[-\*] \[(.+)\]\s*\(.*?\/([a-z0-9]{6})[\/\)]")

    line_splits = re.split(r"\n\s*----*\s*\n", text)
    category = line_splits[0]
    title = line_splits[1]
    metainfo = line_splits[3]
    tabletitle = line_splits[4]
    tabletext = "\n".join(line_splits[4:])

    meta_split = re.split("\*\*(.*):\*\*", metainfo)
    i=1
    description = ""
    use_with_text = ""
    related_text = ""
    keywords_text = ""
    while(i < len(meta_split)):
        if meta_split[i].lower() == "suggested use":
            description = meta_split[i+1].strip()
        elif meta_split[i].lower() == "use these tables with":
            use_with_text = meta_split[i+1].strip()
        elif meta_split[i].lower() == "related tables":
            related_text = meta_split[i+1].strip()
        elif meta_split[i].lower() == "keywords":
            keywords_text = meta_split[i+1].strip()
        i+=2

    json_dict = {}
    json_dict['title'] = title
    json_dict['category'] = category
    json_dict['description'] = description
    json_dict['keywords'] = list(map(lambda t: t.rstrip("."), keywords_text.split(", ")))
    json_dict['use_with'] = [{"title": m.group(1), "link": m.group(2)} for m in [r_link.match(r) for r in use_with_text.split("\n")] if m]
    json_dict['related_tables'] = [{"title": m.group(1), "link": m.group(2)} for m in [r_link.match(r) for r in related_text.split("\n")] if m]
    json_dict['id'] = postid
    json_dict['reference'] = "https://www.reddit.com/r/BehindTheTables/comments/" + json_dict['id']

    tables = []

    if postid == "debug":
        import pdb; pdb.set_trace()

    for line in tabletext.split("\n"):
        match = r_category.match(line)
        if match:
            pass
        match = r_title.match(line)
        if match:
            tables.append({'name': match.group(2), 'dice': match.group(1), 'table_entries': []})
            continue
        match = r_item.match(line)
        if match:
            tables[-1]['table_entries'].append({'entry': match.group(2), 'dice_val': match.group(1)})
    json_dict['tables'] = tables
    with open("tables_json/table_" + json_dict['id'] + ".json", 'w', encoding="utf-8") as outfile:
        json.dump(json_dict, outfile, sort_keys=True, indent=4)

def replace_line(input_text, line_num, text):
    l = input_text.split("\n")
    l[line_num-1] = text
    return "\n".join(l)

def static_fixes(text, postid):
    # Shame on these

    # Someone forgot to add a divider between the infos and the tables...
    if postid == "41vhpr":
        return replace_line(text, 28, "----")

    # 439amj just can't be saved, sorry

    # Again, the divider
    if postid == "4psde0":
        return replace_line(text, 28, "----")

    if postid == "4pxgzh":
        return replace_line(text, 28, "----")

    if postid == "4xln3t":
        return replace_line(text, 41, "----")

    # Divider and Link back missing
    if postid == "567owq":
        text = replace_line(text, 30, "----\n")
        text = replace_line(text, 28, "----\n")
        text = replace_line(text, 4, "----\nStuff\n----")
        return text

    # Someone forgot the dice value
    if postid == "45uoqf":
        text = replace_line(text, 33, "**d20 Dwarf Enclave**")
        text = replace_line(text, 57, "**d20 Elf Enclave**")
        text = replace_line(text, 80, "**d20 Orc Enclave**")
        text = replace_line(text, 104, "**d20 Gnome Enclave**")
        return text

    # This one got a little too fancy with its description, so we just cut it out for now
    if postid == "3shczv":
        return replace_line(text, 33, " ")

    # Get your table titles right m8
    if postid == "40r638":
        text = replace_line(text, 36, "")
        text = replace_line(text, 36, "**D10 Source**")
        text = replace_line(text, 51, "**D20 Descriptor 1**")
        text = replace_line(text, 76, "**D100 Place**")
        text = replace_line(text, 182, "**D20 Descriptor 2**")
        text = replace_line(text, 207, "**D20 Area - *somewhere* **")
        text = replace_line(text, 232, "**D10 Situation - *If asked further, people will tell you, that***")
        text = replace_line(text, 247, "**D6 Person**")
        text = replace_line(text, 258, "**D20 Action**")

    return text



for filename in os.listdir('tables/'):
    with open('tables/' + filename, encoding="utf-8") as infile:
        try:
            convert_md_to_json(infile)
            print("Converted " + filename)
        except Exception as e:
            print("Failure to convert in file " + filename)
            
            print(traceback.format_exc())