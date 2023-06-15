#!/usr/bin/python

import json
from os import getenv, listdir
from os.path import isfile, join

src = getenv('GRAVITON_DATASOURCE_FS_SUBSCRIPTIONS', 'config/subscription_data/subscriptions')

filelist = [f[:-4] for f in listdir(src) if isfile(join(src, f))]

def chunks(lst, n):
    """Yield successive n-sized chunks from lst."""
    for i in range(0, len(lst), n):
        yield lst[i:i + n]

block_size = int(len(filelist) / 10) + 1

print("Count: {}, block_size: {}".format(len(filelist), block_size))

org_lists = list(chunks(filelist, block_size))

count = 0
elements = 0
for org_list in org_lists:
    with open("org_block-{}.json".format(count), 'w') as outfile:
        elements = elements + len(org_list)
        json.dump(org_list, outfile)
        count = count + 1

print("Done. Total elements written: {}".format(elements))
