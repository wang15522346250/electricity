package com.atguigu.gmall.util;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class ArrayList {

}
/*arraylist继承了abstractList，实现了List,它是一个数组队列，提供了相关的添加，删除，修改，遍历等功能。
 实现类RandomAccess接口说明提供了随机访问功能。RandmoAccess是java中用来被List实现的，为List提供快速访问功能的。
在ArrayList中，可以通过元素的序号快速查找到该元素对象。 实现了Cloneable接口，说明可以复制克隆。一般用
Arrays.copyOf(要拷贝的数组，数组大小，数组类型。)
System提供了一个静态方法，System.arraycopy(原数组src,原数组要复制的起始位置，目的数组，目的数组所放置的起始位置，复制长度)
System.arraycopy的数组原型：public static void (Object src,
                             int srcPos,
                             Object dest,
                             int destPos,
                             int length)

  AbstractList.ListItr 的优化版本
     * ListIterator 与普通的 Iterator 的区别：
     * - 它可以进行双向移动，而普通的迭代器只能单向移动
     * - 它可以添加元素(有 add() 方法)，而后者不行
  每增加一个元素add(),在插入索引元素之后的元素，都会往后移动一个单位
  每移除一个元素remove()，移除元素之后的元素都会向左移动一位，并且最后一位设置为Null，以便于GC垃圾回收机制处理
实现了serializable接口，说明了该数组可以序列化。
个人理解：Arraylist在jdk1.7之前，数组长度默认为10，然后扩容算法：int newCapacity=(oldCapacity*3)/2+1.判断是否扩容，假如扩容，
扩容长度为：原来的1.5倍+1
ArrayList在JDK1.7以后扩容更新使用了位运算符，即int newCapacity=oldCapacity+(oldCapacity>>1).右位运算符移1位，相当于/2
ArraList在JDK1.8新特性初始化数组长度为{}即为0.
  private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

注意：从数组中移除元素的操作，也会导致被移除的元素以后的所有元素的向左移动一个位置

 */
/*
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
    //唯一标识符
    private static final long serialVersionUID = 8683452581122892189L;
    //默认容器容量为10
    private static final int DEFAULT_CAPACITY = 10;
    //默认数组对象为Null,用于赋默认值
    private static final Object[] EMPTY_ELEMENTDATA = {};
    //1.8新特性，默认数组容量为0
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
    //定义一个不需要事务的变量数组
    transient Object[] elementData;
    //数组的大小
    private int size;

    //定义初始化数组容量，根据传递的参数决定
    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: " +
                    initialCapacity);
        }
    }

    //默认的初始化数组容量为0
    public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    //构造一个包含指定 collection 的元素的列表，这些元素按照该collection的迭代器返回它们的顺序排列的。
    public ArrayList(Collection<? extends E> c) {
        //collection c.toArray()转变为数组
        elementData = c.toArray();
        //如果该数组的长度不等于0
        if ((size = elementData.length) != 0) {
            // c.toArray might (incorrectly) not return Object[] (see 6260652)
            //如果该元素的class与之前命名的transient Object[] elementData的class类型不一致
            if (elementData.getClass() != Object[].class)
                //调用Arrays.copyOf方法实现复制。
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {
            // replace with empty array.
            //如果数组长度为0.则该元素=0
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }

    //修剪ArrayList的容量大小为size
    public void trimToSize() {
        modCount++;
        //判断arraylist的容量大小是否小于指定元素的容量大小
        if (size < elementData.length) {
            //如果指定元素的大小为0
            elementData = (size == 0)
                    //设置为空
                    ? EMPTY_ELEMENTDATA
                    //如果不为0，则复制elementData，size复制的大小数
                    : Arrays.copyOf(elementData, size);
        }
    }

    //确定容器大小 或者理解为传递参数设置容器大小
    public void ensureCapacity(int minCapacity) {
        //如果指定元素与默认的元素不相等
        //三元表达式
        int minExpand = (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
                // any size if not default element table
                //数组长度设置为0
                ? 0
                // larger than default for default empty table. It's already
                // supposed to be at default size.
                //否则设置为默认容量大小为10
                : DEFAULT_CAPACITY;
        //判断如果最小容量大于初始容量
        if (minCapacity > minExpand) {
            //调用扩容方法改变容器容量
            ensureExplicitCapacity(minCapacity);
        }
    }

    //私有方法，初始化容器容量，自定义设值
    private void ensureCapacityInternal(int minCapacity) {
        //如果初始元素==之前设置的初始元素值{}
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }

        ensureExplicitCapacity(minCapacity);
    }

    private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // overflow-conscious code
        //如果传递的数组容量大小>默认元素的长度
        if (minCapacity - elementData.length > 0)
            //调用扩容方法
            grow(minCapacity);
    }

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private void grow(int minCapacity) {
        // overflow-conscious code
        //之前设定的容量=指定元素的长度
        int oldCapacity = elementData.length;
        //设值新的容量大小=旧的容量大小+旧的容量大小/2   相当于扩容1.5倍
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        //如果新的容量大小<之前设定的容量大小
        if (newCapacity - minCapacity < 0)
            //设置为之前设定的容量大小即初始容量大小
            newCapacity = minCapacity;
        //如果新的容量大小>最大容量大小
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            //调用hugeCapacity方法给新容量赋值
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        //复制数组，大小为新的容量
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        //如果初始化容量<0，则抛异常
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        //返回值三元表达式  用户传递的容量大小>系统默认array大小
        return (minCapacity > MAX_ARRAY_SIZE) ?
                //设置最大值
                Integer.MAX_VALUE :
                //否则取原来的array大小
                MAX_ARRAY_SIZE;
    }

    //判断是否为空
    public boolean isEmpty() {
        return size == 0;
    }

    //判断是否包含
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    //调用IndexOf进行判断
    public int indexOf(Object o) {
        //如果为空
        if (o == null) {
            for (int i = 0; i < size; i++)
                if (elementData[i] == null)
                    return i;
        } else {
            //不为空
            for (int i = 0; i < size; i++)
                //找到该对象与该数组值相等匹配的索引
                if (o.equals(elementData[i]))
                    return i;
        }
        //如果不为空，又不匹配的话返回-1
        return -1;
    }

    ///返回arrayList最后出现o的索引
    public int lastIndexOf(Object o) {
        if (o == null) {
            //运用倒起遍历
            for (int i = size - 1; i >= 0; i--)
                if (elementData[i] == null)
                    return i;
        } else {
            //运用倒起遍历
            for (int i = size - 1; i >= 0; i--)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    //克隆方法
    public Object clone() {
        try {
            java.util.ArrayList<?> v = (java.util.ArrayList<?>) super.clone();
            //返回一个克隆出来的数组
            v.elementData = Arrays.copyOf(elementData, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }

    //转变为数组 调用arrays.copyOf拷贝方法
    public Object[] toArray() {
        return Arrays.copyOf(elementData, size);
    }

    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(elementData, size, a.getClass());
        //elementData数据源，0要拷贝的起始位置，a表示拷贝到a数组，0表示拷贝的索引，size表示拷贝数组大小
        System.arraycopy(elementData, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    //根据索引查询出来在该索引中的对象
    E elementData(int index) {
        return (E) elementData[index];
    }

    //通过索引，获取该索引所在位置的对象
    public E get(int index) {
        rangeCheck(index);

        return elementData(index);
    }

    public E set(int index, E element) {
        rangeCheck(index);

        E oldValue = elementData(index);
        elementData[index] = element;
        return oldValue;
    }

    @Override
    public int size() {
        return 0;
    }

    //判断是否可以添加一个元素
    public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        elementData[size++] = e;
        return true;
    }
    //在对应的索引位置中添加元素
    public void add(int index, E element) {
        rangeCheckForAdd(index);

        ensureCapacityInternal(size + 1);  // Increments modCount!!
        System.arraycopy(elementData, index, elementData, index + 1,
                size - index);
        elementData[index] = element;
        size++;
    }
    //移除某个索引中的值
    */
/*
    remove方法
        根据索引remove
        1）判断索引有没有越界

        2）自增修改次数

        3）将指定位置（index）上的元素保存到oldValue

        4）将指定位置（index）上的元素都往前移动一位

        5）将最后面的一个元素置空，好让垃圾回收器回收

        6）将原来的值oldValue返回
     *//*

    public E remove(int index) {
        rangeCheck(index);

        modCount++;
        //根据索引获取它的对象值
        E oldValue = elementData(index);

        int numMoved = size - index - 1;
        if (numMoved > 0)
            //如果能够移除成功，则里面元素向前移动一个位置。
            System.arraycopy(elementData, index+1, elementData, index,
                    numMoved);
        //数组元素-1，如果为空，设置为Null
        elementData[--size] = null; // clear to let GC do its work
        //返回
        return oldValue;
    }
    //删除一个元素
    public boolean remove(Object o) {
        //如果该元素为Null
        if (o == null) {
            //循环遍历，看数组中是否有Null值，如果有，则根据它的索引移除
            for (int index = 0; index < size; index++)
                if (elementData[index] == null) {
                    fastRemove(index);
                    return true;
                }
        } else {
            //如果该元素不为空，则循环遍历看是否有值与之相等
            for (int index = 0; index < size; index++)
                if (o.equals(elementData[index])) {
                //如果相等，则移除索引
                    fastRemove(index);
                    return true;
                }
        }
        return false;
    }
    private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                    numMoved);
        //根据索引拷贝新的数组，然后移除旧的值
        elementData[--size] = null; // clear to let GC do its work
    }
    //清空arraylist中的值，实际上把里面所有的值是设置为Null。
    public void clear() {
        modCount++;

        // clear to let GC do its work
        for (int i = 0; i < size; i++)
            elementData[i] = null;

        size = 0;
    }
    //判断是否可以增加传递的参数
    public boolean addAll(Collection<? extends E> c) {
        Object[] a = c.toArray();
        //数组的长度大小
        int numNew = a.length;
        //调用扩容方法传参扩容
        ensureCapacityInternal(size + numNew);  // Increments modCount
        //复制，复制的大小为新数组的长度
        System.arraycopy(a, 0, elementData, size, numNew);
        size += numNew;
        return numNew != 0;
    }
    //和上面的基本一致，只不过多了判断。
    public boolean addAll(int index, Collection<? extends E> c) {
        //检查是否可以添加。 一般根据索引是否超过数组长度的大小，如果超过，则抛数组下标越界异常
        rangeCheckForAdd(index);
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(size + numNew);  // Increments modCount
        //看添加索引的数量
        int numMoved = size - index;
        if (numMoved > 0)
            System.arraycopy(elementData, index, elementData, index + numNew,
                    numMoved);

        System.arraycopy(a, 0, elementData, index, numNew);
        size += numNew;
        return numNew != 0;
    }
    //删除一行  传递参数：开始索引，结束索引
    protected void removeRange(int fromIndex, int toIndex) {
        modCount++;
        //数组的大小-结束索引的大小获取拷贝数组的长度大小
        int numMoved = size - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex,
                numMoved);

        // clear to let GC do its work
        //调用GC垃圾回收机制回收 设置值为Null相当于把它们移除了
        int newSize = size - (toIndex-fromIndex);
        for (int i = newSize; i < size; i++) {
            elementData[i] = null;
        }
        size = newSize;
    }
    //检验是否数组下标越界
    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
    //抛异常发送的消息
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }
    //删除所有集合元素
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, false);
    }
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        //调用批量删除方法进行删除
        return batchRemove(c, true);
    }
    //是否可以批量删除
    private boolean batchRemove(Collection<?> c, boolean complement) {
        final Object[] elementData = this.elementData;
        int r = 0, w = 0;
        boolean modified = false;
        try {
            for (; r < size; r++)
                //判断该集合是否包含该元素
                //如果包含
                if (c.contains(elementData[r]) == complement)
                    //elementData[1]=elementData[1]
                    elementData[w++] = elementData[r];
        } finally {
            // Preserve behavioral compatibility with AbstractCollection,
            // even if c.contains() throws.
            //如果r!=数组容量大小
            if (r != size) {
                //复制
                System.arraycopy(elementData, r,
                        elementData, w,
                        size - r);
                w += size - r;
            }
            if (w != size) {
                // clear to let GC do its work
                for (int i = w; i < size; i++)
                    //设置为元素为Null，便于垃圾回收机制回收
                    elementData[i] = null;
                modCount += size - w;
                size = w;
                modified = true;
            }
        }
        return modified;
    }
    //用Io流的方式写
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException{
        // Write out element count, and any hidden stuff
        int expectedModCount = modCount;
        s.defaultWriteObject();

        // Write out size as capacity for behavioural compatibility with clone()
        s.writeInt(size);

        // Write out all elements in the proper order.
        for (int i=0; i<size; i++) {
            s.writeObject(elementData[i]);
        }

        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }
    //以io流的方式读
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        elementData = EMPTY_ELEMENTDATA;

        // Read in size, and any hidden stuff
        //读取大小
        s.defaultReadObject();

        // Read in capacity
        s.readInt(); // ignored

        if (size > 0) {
            // be like clone(), allocate array based upon size not capacity
            //确认容量大小
            ensureCapacityInternal(size);

            Object[] a = elementData;
            // Read in all elements in the proper order.
            for (int i=0; i<size; i++) {
                a[i] = s.readObject();
            }
        }
    }
    //Iterator迭代器
    public ListIterator<E> listIterator(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: "+index);
        return new java.util.ArrayList.ListItr(index);
    }
    public ListIterator<E> listIterator() {
        return new java.util.ArrayList.ListItr(0);
    }

    public Iterator<E> iterator() {
        return new java.util.ArrayList.Itr();
    }

    private class Itr implements Iterator<E> {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;
        //判断是否有下一个元素
        public boolean hasNext() {
            return cursor != size;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            checkForComodification();
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            Object[] elementData = java.util.ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return (E) elementData[lastRet = i];
        }
        //移除
        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                java.util.ArrayList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);
            final int size = java.util.ArrayList.this.size;
            int i = cursor;
            if (i >= size) {
                return;
            }
            final Object[] elementData = java.util.ArrayList.this.elementData;
            if (i >= elementData.length) {
                throw new ConcurrentModificationException();
            }
            while (i != size && modCount == expectedModCount) {
                consumer.accept((E) elementData[i++]);
            }
            // update once at end of iteration to reduce heap write traffic
            cursor = i;
            lastRet = i - 1;
            checkForComodification();
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    */
/**
 * An optimized version of AbstractList.ListItr
 * <p>
 * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
 * and <em>fail-fast</em> {@link Spliterator} over the elements in this
 * list.
 * <p>
 * <p>The {@code Spliterator} reports {@link Spliterator#SIZED},
 * {@link Spliterator#SUBSIZED}, and {@link Spliterator#ORDERED}.
 * Overriding implementations should document the reporting of additional
 * characteristic values.
 *
 * @return a {@code Spliterator} over the elements in this list
 * @since 1.8
 * Index-based split-by-two, lazily initialized Spliterator  Create new spliterator covering the given  range
 *//*

    private class ListItr extends java.util.ArrayList.Itr implements ListIterator<E> {
        ListItr(int index) {
            super();
            cursor = index;
        }
        //前面是否有值
        public boolean hasPrevious() {
            return cursor != 0;
        }
        //下一个索引
        public int nextIndex() {
            return cursor;
        }
        //上一个索引
        public int previousIndex() {
            return cursor - 1;
        }

        @SuppressWarnings("unchecked")
        public E previous() {
            checkForComodification();
            int i = cursor - 1;
            if (i < 0)
                throw new NoSuchElementException();
            Object[] elementData = java.util.ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i;
            return (E) elementData[lastRet = i];
        }

        public void set(E e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                java.util.ArrayList.this.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(E e) {
            checkForComodification();

            try {
                int i = cursor;
                java.util.ArrayList.this.add(i, e);
                cursor = i + 1;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    public List<E> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size);
        return new java.util.ArrayList.SubList(this, 0, fromIndex, toIndex);
    }

    static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size)
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                    ") > toIndex(" + toIndex + ")");
    }

    private class SubList extends AbstractList<E> implements RandomAccess {
        private final AbstractList<E> parent;
        private final int parentOffset;
        private final int offset;
        int size;

        SubList(AbstractList<E> parent,
                int offset, int fromIndex, int toIndex) {
            this.parent = parent;
            this.parentOffset = fromIndex;
            this.offset = offset + fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = java.util.ArrayList.this.modCount;
        }

        public E set(int index, E e) {
            rangeCheck(index);
            checkForComodification();
            E oldValue = java.util.ArrayList.this.elementData(offset + index);
            java.util.ArrayList.this.elementData[offset + index] = e;
            return oldValue;
        }

        public E get(int index) {
            rangeCheck(index);
            checkForComodification();
            return java.util.ArrayList.this.elementData(offset + index);
        }

        public int size() {
            checkForComodification();
            return this.size;
        }

        public void add(int index, E e) {
            rangeCheckForAdd(index);
            checkForComodification();
            parent.add(parentOffset + index, e);
            this.modCount = parent.modCount;
            this.size++;
        }

        public E remove(int index) {
            rangeCheck(index);
            checkForComodification();
            E result = parent.remove(parentOffset + index);
            this.modCount = parent.modCount;
            this.size--;
            return result;
        }

        protected void removeRange(int fromIndex, int toIndex) {
            checkForComodification();
            parent.removeRange(parentOffset + fromIndex,
                    parentOffset + toIndex);
            this.modCount = parent.modCount;
            this.size -= toIndex - fromIndex;
        }

        public boolean addAll(Collection<? extends E> c) {
            return addAll(this.size, c);
        }

        public boolean addAll(int index, Collection<? extends E> c) {
            rangeCheckForAdd(index);
            int cSize = c.size();
            if (cSize==0)
                return false;

            checkForComodification();
            parent.addAll(parentOffset + index, c);
            this.modCount = parent.modCount;
            this.size += cSize;
            return true;
        }

        public Iterator<E> iterator() {
            return listIterator();
        }

        public ListIterator<E> listIterator(final int index) {
            checkForComodification();
            rangeCheckForAdd(index);
            final int offset = this.offset;

            return new ListIterator<E>() {
                int cursor = index;
                int lastRet = -1;
                int expectedModCount = java.util.ArrayList.this.modCount;

                public boolean hasNext() {
                    return cursor != java.util.ArrayList.SubList.this.size;
                }

                @SuppressWarnings("unchecked")
                public E next() {
                    checkForComodification();
                    int i = cursor;
                    if (i >= java.util.ArrayList.SubList.this.size)
                        throw new NoSuchElementException();
                    Object[] elementData = java.util.ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i + 1;
                    return (E) elementData[offset + (lastRet = i)];
                }

                public boolean hasPrevious() {
                    return cursor != 0;
                }

                @SuppressWarnings("unchecked")
                public E previous() {
                    checkForComodification();
                    int i = cursor - 1;
                    if (i < 0)
                        throw new NoSuchElementException();
                    Object[] elementData = java.util.ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i;
                    return (E) elementData[offset + (lastRet = i)];
                }

                @SuppressWarnings("unchecked")
                public void forEachRemaining(Consumer<? super E> consumer) {
                    Objects.requireNonNull(consumer);
                    final int size = java.util.ArrayList.SubList.this.size;
                    int i = cursor;
                    if (i >= size) {
                        return;
                    }
                    final Object[] elementData = java.util.ArrayList.this.elementData;
                    if (offset + i >= elementData.length) {
                        throw new ConcurrentModificationException();
                    }
                    while (i != size && modCount == expectedModCount) {
                        consumer.accept((E) elementData[offset + (i++)]);
                    }
                    // update once at end of iteration to reduce heap write traffic
                    lastRet = cursor = i;
                    checkForComodification();
                }

                public int nextIndex() {
                    return cursor;
                }

                public int previousIndex() {
                    return cursor - 1;
                }

                public void remove() {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        java.util.ArrayList.SubList.this.remove(lastRet);
                        cursor = lastRet;
                        lastRet = -1;
                        expectedModCount = java.util.ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void set(E e) {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        java.util.ArrayList.this.set(offset + lastRet, e);
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void add(E e) {
                    checkForComodification();

                    try {
                        int i = cursor;
                        java.util.ArrayList.SubList.this.add(i, e);
                        cursor = i + 1;
                        lastRet = -1;
                        expectedModCount = java.util.ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                final void checkForComodification() {
                    if (expectedModCount != java.util.ArrayList.this.modCount)
                        throw new ConcurrentModificationException();
                }
            };
        }

        */
/*public List<E> subList(int fromIndex, int toIndex) {
            subListRangeCheck(fromIndex, toIndex, size);
            return new java.util.ArrayList.SubList(this, offset, fromIndex, toIndex);
        }*//*


        private void rangeCheck(int index) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private void rangeCheckForAdd(int index) {
            if (index < 0 || index > this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

   */
/*     private String outOfBoundsMsg(int index) {
            return "Index: "+index+", Size: "+this.size;
        }

        private void checkForComodification() {
            if (java.util.ArrayList.this.modCount != this.modCount)
                throw new ConcurrentModificationException();
        }

        public Spliterator<E> spliterator() {
            checkForComodification();
            return new java.util.ArrayList.ArrayListSpliterator<E>(java.util.ArrayList.this, offset,
                    offset + this.size, this.modCount);
        }
    }
*//*

    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        final int expectedModCount = modCount;
        @SuppressWarnings("unchecked")
        final E[] elementData = (E[]) this.elementData;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            action.accept(elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    */
/**
 * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
 * and <em>fail-fast</em> {@link Spliterator} over the elements in this
 * list.
 *
 * <p>The {@code Spliterator} reports {@link Spliterator#SIZED},
 * {@link Spliterator#SUBSIZED}, and {@link Spliterator#ORDERED}.
 * Overriding implementations should document the reporting of additional
 * characteristic values.
 *
 * @return a {@code Spliterator} over the elements in this list
 * @since 1.8
 *//*

*/
/*    @Override
    public Spliterator<E> spliterator() {
        return new java.util.ArrayList.ArrayListSpliterator<>(this, 0, -1, 0);
    }

    *//*
*/
/** Index-based split-by-two, lazily initialized Spliterator *//*
*/
/*
    static final class ArrayListSpliterator<E> implements Spliterator<E> {

        *//*
*/
/*
         * If ArrayLists were immutable, or structurally immutable (no
         * adds, removes, etc), we could implement their spliterators
         * with Arrays.spliterator. Instead we detect as much
         * interference during traversal as practical without
         * sacrificing much performance. We rely primarily on
         * modCounts. These are not guaranteed to detect concurrency
         * violations, and are sometimes overly conservative about
         * within-thread interference, but detect enough problems to
         * be worthwhile in practice. To carry this out, we (1) lazily
         * initialize fence and expectedModCount until the latest
         * point that we need to commit to the state we are checking
         * against; thus improving precision.  (This doesn't apply to
         * SubLists, that create spliterators with current non-lazy
         * values).  (2) We perform only a single
         * ConcurrentModificationException check at the end of forEach
         * (the most performance-sensitive method). When using forEach
         * (as opposed to iterators), we can normally only detect
         * interference after actions, not before. Further
         * CME-triggering checks apply to all other possible
         * violations of assumptions for example null or too-small
         * elementData array given its size(), that could only have
         * occurred due to interference.  This allows the inner loop
         * of forEach to run without any further checks, and
         * simplifies lambda-resolution. While this does entail a
         * number of checks, note that in the common case of
         * list.stream().forEach(a), no checks or other computation
         * occur anywhere other than inside forEach itself.  The other
         * less-often-used methods cannot take advantage of most of
         * these streamlinings.
         *//*
*/
/*

        private final java.util.ArrayList<E> list;
        private int index; // current index, modified on advance/split
        private int fence; // -1 until used; then one past last index
        private int expectedModCount; // initialized when fence set

        *//*
*/
/** Create new spliterator covering the given  range *//*
*/
/*
        ArrayListSpliterator(java.util.ArrayList<E> list, int origin, int fence,
                             int expectedModCount) {
            this.list = list; // OK if null unless traversed
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }*//*

*/
/*
        private int getFence() { // initialize fence to size on first use
            int hi; // (a specialized variant appears in method forEach)
            java.util.ArrayList<E> lst;
            if ((hi = fence) < 0) {
                if ((lst = list) == null)
                    hi = fence = 0;
                else {
                    expectedModCount = lst.modCount;
                    hi = fence = lst.size;
                }
            }
            return hi;
        }

        public java.util.ArrayList.ArrayListSpliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null : // divide range in half unless too small
                    new java.util.ArrayList.ArrayListSpliterator<E>(list, lo, index = mid,
                            expectedModCount);
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            int hi = getFence(), i = index;
            if (i < hi) {
                index = i + 1;
                @SuppressWarnings("unchecked") E e = (E)list.elementData[i];
                action.accept(e);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi, mc; // hoist accesses and checks from loop
            java.util.ArrayList<E> lst; Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((lst = list) != null && (a = lst.elementData) != null) {
                if ((hi = fence) < 0) {
                    mc = lst.modCount;
                    hi = lst.size;
                }
                else
                    mc = expectedModCount;
                if ((i = index) >= 0 && (index = hi) <= a.length) {
                    for (; i < hi; ++i) {
                        @SuppressWarnings("unchecked") E e = (E) a[i];
                        action.accept(e);
                    }
                    if (lst.modCount == mc)
                        return;
                }
            }
            throw new ConcurrentModificationException();
        }*//*


        public long estimateSize() {
            return (long) (getFence() - index);
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        // figure out which elements are to be removed
        // any exception thrown from the filter predicate at this stage
        // will leave the collection unmodified
        int removeCount = 0;
        final BitSet removeSet = new BitSet(size);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            @SuppressWarnings("unchecked")
            final E element = (E) elementData[i];
            if (filter.test(element)) {
                removeSet.set(i);
                removeCount++;
            }
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }

        // shift surviving elements left over the spaces left by removed elements
        final boolean anyToRemove = removeCount > 0;
        if (anyToRemove) {
            final int newSize = size - removeCount;
            for (int i=0, j=0; (i < size) && (j < newSize); i++, j++) {
                i = removeSet.nextClearBit(i);
                elementData[j] = elementData[i];
            }
            for (int k=newSize; k < size; k++) {
                elementData[k] = null;  // Let gc do its work
            }
            this.size = newSize;
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            modCount++;
        }

        return anyToRemove;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            elementData[i] = operator.apply((E) elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super E> c) {
        final int expectedModCount = modCount;
        Arrays.sort((E[]) elementData, 0, size, c);
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }
}


}

*/
