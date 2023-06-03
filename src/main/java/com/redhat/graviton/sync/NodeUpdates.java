package com.redhat.graviton.sync;

import java.util.HashSet;
import java.util.Set;



// This should not accept raw strings for the field names, nor should this class be usable directly.
// It should instead be something like an abstract class which has a type that defines what kind
// of fields can be defined (enum + iface) so the fields are well-defined somewhere.

// Maybe all of this is extraneous, but the general goal is to know which fields are updated (or
// in some other contexts, present at all). If there's a better solution that can cover both needs,
// that should be explored here

public class NodeUpdates {

    private static final class EmptyNodeUpdates extends NodeUpdates {

        public EmptyNodeUpdates() {
            // intentionally left empty
        }

        public NodeUpdates addField(String fieldName) {
            // Interface needs work if we're doing this. NodeUpdates should probably be more
            // immutable.
            throw new UnsupportedOperationException();
        }

        public boolean isFieldUpdated(String fieldName) {
            return false;
        }

        public int count() {
            return 0;
        }

    }

    public static final NodeUpdates EMPTY_UPDATES = new EmptyNodeUpdates();




    // This field currently stores only field names, but could later be updated to be a map from
    // something like a field name to an update object which contains the original and updated
    // values where appropriate. Maybe not worth the memory footprint, and won't be part of the poc
    private final Set<String> updates;


    public NodeUpdates() {
        this.updates = new HashSet<>();
    }

    public NodeUpdates addField(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            throw new IllegalArgumentException("field name is null or empty");
        }

        this.updates.add(fieldName);
        return this;
    }

    public int count() {
        return this.updates.size();
    }

    public String toString() {
        return String.format("NodeUpdates: %s", this.updates);
    }

}
