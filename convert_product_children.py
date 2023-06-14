#!/usr/bin/python

import json
from os import listdir
from os.path import isfile, join

src = "/home/crog/devel/subscription_data/product_data"

filelist = [f for f in listdir(src) if isfile(join(src, f)) and f.endswith("-tree.json")]


for filename in filelist:
    with open(join(src, filename), 'r') as treefile:
        tree = json.load(treefile)

        provided = tree['providedOids']
        derived = tree['derivedOids']

        children = {
            'productId': tree['oid'],
            'childrenProductIds': {}
        }

        if provided is not None and len(provided) > 0:
            children['childrenProductIds']['provided'] = provided

        if derived is not None and len(derived) > 0:
            children['childrenProductIds']['derived'] = derived

        print("TREE: {}".format(tree))
        print("CHILDREN: {}".format(children))

        with open(join(src, filename[:-10] + '-children.json'), 'w') as childrenfile:
            json.dump(children, childrenfile)
