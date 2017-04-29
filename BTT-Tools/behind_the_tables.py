# -*- coding: utf-8 -*-
import re
import json
import os
import traceback

meta_dict = {}

def convert_md_to_json(textfile):
    text = textfile.read()

    postid = textfile.name.split(".")[0].split("/")[-1]

    text = static_fixes(text, postid)

    r_title = re.compile(r"\s*\*\*(\d*[dD]\d+)\.?[\s\*]+(.+)\*\*")
    r_category = re.compile(r"^\s*\*\*(.+)\*\*\s*$")
    r_category2 = re.compile(r"^\s*#+\s*(.+)\s*$")
    r_item = re.compile(r"(\d+-?\d*)\.\s*(.+)")

    r_link = re.compile(r"[-\*] \[(.+)\]\s*\(.*?\/([a-z0-9]{6})[\/\)]")
    r_meta_header = re.compile(r"\*\*(.*?):?\*\*\s*?\n")

    line_splits = re.split(r"\n\s*----*\s*\n", text)
    category = line_splits[0]
    title = line_splits[1]
    metainfo = line_splits[3]
    tabletitle = line_splits[4]
    tabletext = "\n".join(line_splits[4:])

    meta_split = r_meta_header.split(metainfo)
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

    if postid == "debug":
        import pdb; pdb.set_trace()

    tables = []

    for line in tabletext.split("\n"):
        match = r_title.match(line)
        if match:
            tables.append({'name': match.group(2).strip(), 'dice': match.group(1).strip(), 'table_entries': []})
            continue
        match = r_category.match(line)
        if match:
            subcategory = match.group(1)
            # If a subcategory is all caps, make it title case
            if subcategory.isupper():
                subcategory = subcategory.title()
            tables.append({'subcategory': subcategory.strip()})
            continue
        match = r_item.match(line)
        if match:
            tables[-1]['table_entries'].append({'entry': match.group(2), 'dice_val': match.group(1)})
    
    json_dict['tables'] = tables

    # Fix numbering on consecutive entries (because markdown allows any sequence of numbers, but we dont)
    for table in json_dict["tables"]:
        if "table_entries" in table:
            fix_numbering(table)

    # Do a quick check if the dice numbers match the number of entries
    for table in json_dict["tables"]:
        if "table_entries" in table:
            dicenumber = int(table["dice"].lower().split("d")[-1])
            tablenumber = len(table['table_entries'])
            last_entry_number = int(table['table_entries'][-1]['dice_val'].split("-")[-1])
            if not dicenumber == tablenumber and not dicenumber == last_entry_number:
                print("Warning! In table {} for entry {}, dice number ({}) is not equal to the number of entries ({})".format(postid, table['name'], dicenumber, tablenumber))

    with open("tables_json/table_" + json_dict['id'] + ".json", 'w', encoding="utf-8") as outfile:
        json.dump(json_dict, outfile, sort_keys=True, indent=4, ensure_ascii=False)

    # Add to meta_table
    meta = {"keywords": json_dict["keywords"],
            "description": json_dict["description"],
            "title": json_dict["title"],
            "related_tables": [uw['link'] for uw in json_dict["related_tables"]],
            "use_with": [uw['link'] for uw in json_dict["use_with"]],
            "category": json_dict["category"]}
    meta_dict["table_" + json_dict["id"]] = meta

def fix_numbering(table):
    table_entries = table['table_entries']
    tablenumber = len(table_entries)
    # Check if all dice values are single values, return if not
    try:
        for table_entry in table_entries:
            int(table_entry['dice_val'])
    except ValueError:
        return

    # If we don't start at 1, it's probably a multi-dice table
    if int(table_entries[0]['dice_val']) != 1:
        return
    # Check if last entry is not equal to the length of the table
    if int(table_entries[-1]['dice_val']) != tablenumber:
        # If yes, rebuild dice values
        for i, table_entry in enumerate(table_entries):
            table_entry['dice_val'] = str(i + 1)


def replace_line(input_text, line_num, text):
    l = input_text.split("\n")
    l[line_num-1] = text
    return "\n".join(l)

def static_fixes(text, postid):
    # Shame on these

    # Someone forgot to add a divider between the infos and the tables...
    if postid == "41vhpr":
        text = replace_line(text, 28, "----")

    # 439amj just can't be saved, sorry

    # Again, the divider
    if postid == "4psde0":
        text = replace_line(text, 28, "----")

    if postid == "4pxgzh":
        text = replace_line(text, 28, "----")

    if postid == "4xln3t":
        text = replace_line(text, 41, "----")

    # Divider and Link back missing
    if postid == "567owq":
        text = replace_line(text, 30, "----\n")
        text = replace_line(text, 28, "----\n")
        text = replace_line(text, 4, "----\nStuff\n----")

    # Someone forgot the dice value
    if postid == "45uoqf":
        text = replace_line(text, 33, "**d20 Dwarf Enclave**")
        text = replace_line(text, 57, "**d20 Elf Enclave**")
        text = replace_line(text, 80, "**d20 Orc Enclave**")
        text = replace_line(text, 104, "**d20 Gnome Enclave**")

    # This one got a little too fancy with its description, so we just cut it out for now
    if postid == "3shczv":
        text = replace_line(text, 33, " ")

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

    # Put a dice number bro
    if postid == "4xlnp7":
        text = replace_line(text, 148, "**d6 Sub-divisions: To manage the nation, it is split into multiple...**")

    # That table has no entries!
    if postid == "3vlzrw":
        text = replace_line(text, 40, "")

    # Encoding failed
    if postid == "435qlc":
        text = replace_line(text, 3, "Reputations & Rumors")

    # Title spans two lines, and I don't want to rewrite the whole parsing functions to work on that
    if postid == "3tielc":
        text = replace_line(text, 161, "**d6 Who resides in the stone house now?**")
        text = replace_line(text, 162, "")

    # Missing stars around title
    if postid == "41vhpr":
        text = replace_line(text, 59, "**d12 The shaft is made of...**")

    # That's a nice preamble, but not so nice for this purpose
    if "*I am challenging myself" in text:
        l = text.split("\n")
        for linenum, line in enumerate(l):
            if line.startswith("*I am challenging myself") or line.startswith("*I may come back"):
                l[linenum] = ""
        text = "\n".join(l)

    return text



for filename in os.listdir('tables/'):
    with open('tables/' + filename, encoding="utf-8") as infile:
        try:
            convert_md_to_json(infile)
            print("Converted " + filename)
        except Exception as e:
            print("Failure to convert in file " + filename)
            
            print(traceback.format_exc())

with open('tables_json/tables_meta.json', 'w', encoding='utf-8') as meta_file:
    json.dump(meta_dict, meta_file, sort_keys=True, indent="\t", ensure_ascii=False)