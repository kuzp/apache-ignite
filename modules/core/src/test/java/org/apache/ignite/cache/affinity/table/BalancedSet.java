package org.apache.ignite.cache.affinity.table;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO implement borrow with reference to next item ( first.borrowable.length differs on 1 with second.borrowable.length
public class BalancedSet {
    private final int segments;
    private final int backups;

    private int movements = 0;

    private Map<Object, Mapping> map = new HashMap<>();

    private List<Object> owners = new ArrayList<>();

    public BalancedSet(int segments, int backups) {
        this.segments = segments;
        this.backups = backups;
    }

    private class Mapping {
        final Object owner;

        final BitSet primary = new BitSet();

        final BitSet backup = new BitSet();

        public Mapping(Object owner) {
            this.owner = owner;
        }

        @Override public String toString() {
            return "Mapping: [owner=" + owner + ", primary=" + primary + ", backup=" + backup + "" + ']';
        }

        public int borrow() {
            int i = primary.nextSetBit(0);

            primary.clear(i);

            return i;
        }

        public int borrow(BitSet exclude) {
            for (int i = primary.nextSetBit(0); i >= 0; i = primary.nextSetBit(i+1)) {
                if (!exclude.get(i)) {
                    primary.clear(i);

                    return i;
                }
            }

            return -1;
        }


        public void add(int part) {
            assert !primary.get(part);

            primary.set(part);
        }

        public void setBackup(BitSet src) {
            this.backup.clear();

            this.backup.or(src);
        }

        public int borrowFromBackup(BitSet exclude) {
            for (int i = backup.nextSetBit(0); i >= 0; i = backup.nextSetBit(i+1)) {
                if (!exclude.get(i)) {
                    backup.clear(i);

                    return i;
                }
            }

            return -1;
        }

        public void addBackup(int part) {
            backup.set(part);
        }
    }

    public static void main(String[] args) {
        BalancedSet s = new BalancedSet(10, 1);

        s.addOwner("owner0");
        System.out.println(s);

        s.addOwner("owner1");
        System.out.println(s);

        s.addOwner("owner2");
        System.out.println(s);

        s.addOwner("owner3");
        System.out.println(s);

//        s.addOwner("owner4");
//        System.out.println(s);

//        s.addOwner("owner5");
//        System.out.println(s);
//
//        s.addOwner("owner6");
//        System.out.println(s);

//        s.removeOwner("owner1");
//        System.out.println(s);

//        for (int i = 0; i < 9; i++) {
//            s.addOwner("owner" + i);
//            System.out.println(s);
//        }
    }

    private Mapping getMapping(Object owner) {
        return map.get(owner);
    }

    private void removeOwner(String owner) {
        final Mapping removed = map.remove(owner);

        owners.remove(owner);

        if (removed == null)
            return;

        // divide primary and backups on remaining nodes.
        // old backups must become primary.
        // backups are moved on most underloaded nodes.

        final BitSet primary = removed.primary;

        List<Mapping> affected = new ArrayList<>();

        for (int i = primary.nextSetBit(0); i >= 0; i = primary.nextSetBit(i+1)) {
            for (Mapping mapping : map.values()) {
                if (mapping.backup.get(i)) {
                    mapping.backup.clear(i);

                    mapping.primary.set(i);

                    if (!affected.contains(mapping))
                        affected.add(mapping);
                }
            }
        }

        // Sort by cardinality desc.
        Collections.sort(affected, new Comparator<Mapping>() {
            @Override public int compare(Mapping o1, Mapping o2) {
                return o2.primary.cardinality() - o1.primary.cardinality();
            }
        });

        int cur = 0;

        List<Object> other = new ArrayList<>(owners);

        int div = segments / owners.size();

        for (Mapping mapping : affected)
            other.remove(mapping.owner);

        // sort by cardinality
        for (int i = 0; i < affected.size(); i++) {
            final Mapping from = affected.get(i);

            // TODO get more from affected if possible

            Object own = other.get(cur);

            Mapping to = map.get(own);

            int part = from.borrow(removed.primary);

            if (part == -1)
                continue;

            to.add(part);

            cur++;

            cur %= other.size();

            if (to.primary.cardinality() == div)
                break;
        }

        // divide primary and backup from left node
    }

    private void addOwner(Object owner) {
        if (map.containsKey(owner))
            return;

        if (map.size() == 0) {
            Mapping mapping = new Mapping(owner);

            map.put(owner, mapping);

            for (int i = 0; i < segments; i++)
                mapping.primary.set(i);
        }
        else {
            int x = map.size() + 1;

            int div = segments / x;

            Mapping n = new Mapping(owner);
            if (div != 0) {
                int cur = 0;

                List<Object> ordered = new ArrayList<>(owners);

                // Sort by cardinality to borrow first from largest cardinality.
                Collections.sort(ordered, new Comparator<Object>() {
                    @Override public int compare(Object o1, Object o2) {
                        final int c1 = map.get(o2).primary.cardinality();
                        final int c2 = map.get(o1).primary.cardinality();

                        return c1 - c2;
                    }
                });

                // sort by cardinality
                for (int i = 0; i < segments; i++) {
                    Object own = ordered.get(cur);

                    Mapping mapping = map.get(own);

                    int part = mapping.borrow();

                    n.add(part);

                    cur++;

                    cur %= map.size();

                    if (n.primary.cardinality() == div)
                        break;
                }
            }

            // Process backups.

            if (owners.size() == 1) {
                final Mapping first = map.get(owners.get(0));

                n.setBackup(first.primary);

                first.setBackup(n.primary);
            }
            else {
                List<Object> ordered = new ArrayList<>(owners);

                // Sort by cardinality to borrow first from largest cardinality.
                Collections.sort(ordered, new Comparator<Object>() {
                    @Override public int compare(Object o1, Object o2) {
                        final int c1 = map.get(o2).backup.cardinality();
                        final int c2 = map.get(o1).backup.cardinality();

                        return c1 == c2 ? map.get(o2).primary.cardinality() - map.get(o1).primary.cardinality() : c1 - c2;
                    }
                });

                int cur = 0;

                // sort by cardinality
                for (int i = 0; i < segments; i++) {
                    Object own = ordered.get(cur);

                    Mapping mapping = map.get(own);

                    int part = mapping.borrowFromBackup(n.primary);

                    if (part == -1) {
                        cur++;

                        cur %= map.size();

                        continue;
                    }

                    n.addBackup(part);

                    cur++;

                    cur %= map.size();

                    if (n.backup.cardinality() == div)
                        break;
                }
            }

            map.put(owner, n);
        }

        owners.add(owner);
    }

    @Override public String toString() {
        StringBuilder b = new StringBuilder();

        List<Comparable> l = new ArrayList<Comparable>();

        for (Object owner : owners)
            l.add((Comparable)owner);

        Collections.sort(l);

        for (Comparable comparable : l) {
            Mapping mapping = map.get(comparable);

            b.append(mapping.toString());
            b.append(System.lineSeparator());
        }

        return b.toString();
    }
}