# coding: utf-8

import requests
import json
import time
import re
import traceback

headers= {'User-Agent': "/r/BehindTheTables simple crawler (by /u/Parkuhr)"}


def fetch_table(item):
    postID = item["link"].split("/")[-1]
    category = item["category"]
    print("Requesting Post " + postID + ", Title: " + item["title"])
    try:
        j=requests.get("https://www.reddit.com/r/BehindTheTables/comments/" + postID + ".json", headers=headers)
        selftext = j.json()[0]['data']['children'][0]['data']['selftext']
        title = j.json()[0]['data']['children'][0]['data']['title']
        selftext = selftext.replace("&amp;", "&")
        selftext = selftext.replace("nbsp;", " ")
        title = title.replace("&amp;", "&")
        text_file = open("tables/" + postID + ".md", "w", encoding="utf-8")
        text_file.write(category + "\n----\n" + title + "\n----\n" + selftext)
        text_file.close()
    except Exception as e:
        print("### Could not read " + title + " (https://www.reddit.com/r/BehindTheTables/comments/" + postID + ")")
        traceback.print_exc()
    time.sleep(3)

def fetch_wiki():
    get = requests.get("https://www.reddit.com/r/BehindTheTables/wiki/index.json", headers=headers)
    s=get.json()['data']['content_md']

    r_link = re.compile(r"- \[(.+)\]\s*\(.*?\/([a-z0-9]{6})[\/\)]")

    # Static reworkings:
    s = s.replace("- [Cultists](https://redd.it/40wc26) ([snake cultists](https://redd.it/3uzwi0) variant) [[PDF](https://goo.gl/w54C74)]" ,
                  "- [Cultists](https://redd.it/40wc26) \n- [Snake Cultists](https://redd.it/3uzwi0)")
    s = s.replace("- [Rumor mill](https://redd.it/4q6c0h) ([variant](https://redd.it/51senc))",
                  "- [Rumor mill](https://redd.it/4q6c0h) \n- [Rumor mill (Variant)](https://redd.it/51senc)")
    s = s.replace("- [Nightmares](https://redd.it/43bxpl) ([variant](https://redd.it/439amj))", 
                  "- [Nightmares](https://redd.it/43bxpl) \n- [Nightmares (Variant)](https://redd.it/439amj)")
    s = s.replace("- [Gossip and hearsay](https://redd.it/48spuq) ([variant](https://redd.it/51senc))", 
                  "- [Gossip and hearsay](https://redd.it/48spuq)\n- [Gossip and hearsay (Variant)](https://redd.it/51senc)")
    s = s.replace("- [Swamp](https://redd.it/43c2q8) ([variant](https://redd.it/5xhhlw))", 
                  "- [Swamp](https://redd.it/43c2q8)\n- [Swamp Variant](https://redd.it/5xhhlw)")

    categories = re.split(r"\*\*(.*)\*\*", s)

    i = 3
    linklist = []
    while i < len(categories) - 2:
        category = categories[i]
        text = categories[i+1]
        print("Found category " + category)
        
        for match in re.finditer(r_link, text):
            title = match.group(1)
            link = match.group(2)
            linklist.append({"category": category, "title": title, "link": link})

        i+=2

    return linklist


infile = open("behind_the_tables_links.txt")
lines = infile.readlines()
infile.close()
items = []
for line in lines:
    match = re.match(r'"(.*)": "(.*)"\n', line)
    items.append((match.group(1), match.group(2)))

lst = fetch_wiki()
print(*lst, sep="\n")
time.sleep(3)
for item in lst:
    fetch_table(item)