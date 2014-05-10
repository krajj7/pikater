package org.pikater.core.agents.system.computationDescriptionParser;

import java.util.LinkedList;

/**
 * User: Kuba
 * Date: 10.5.2014
 * Time: 12:07
 */
public class StandardBuffer<E> extends AbstractComputationBuffer<E> {
    LinkedList<E> buffer=new LinkedList<>();

    @Override
    public boolean hasNext() {
        return buffer.size()>0;
    }

    @Override
    public void insert(E element) {
         buffer.addLast(element);
    }

    @Override
    public E getNext() {
        return buffer.getFirst();
    }

    @Override
    public int size() {
        return buffer.size();
    }
}
