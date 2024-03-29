#!/usr/bin/python

from os import getenv
from pathlib import Path

src_dir = getenv('GRAVITON_DATASOURCE_FS_RAW', 'config/subscription_data/raw')
src = Path(src_dir, 'subs.txt')
dest_dir = getenv('GRAVITON_DATASOURCE_FS_SUBSCRIPTIONS', 'config/subscription_data/subscriptions/')


with open(src, 'r') as subs_file:
    # throw out the first line -- it's just our column headers
    subs_file.readline()

    print("Processing subscriptions from {}".format(src))
    count = 0

    while True:
        count += 1
        if (count % 100000 == 0):
            print("Wrote {} lines...".format(count))

        sub_data = subs_file.readline()
        if not sub_data:
            break

        chunks = sub_data.strip().split('\t')
        data_file = dest_dir + chunks[1] + ".txt"

        with open(data_file, 'a') as org_subs:
            org_subs.write(sub_data)

print("done!")
