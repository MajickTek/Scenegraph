package com.sun.scenario.animation;

import java.util.Iterator;

abstract class AnimationList<T extends Animation, N extends AnimationList<T, N>> implements Iterable<N> {
   // $FF: renamed from: t long
   protected long field_23;
   // $FF: renamed from: v com.sun.scenario.animation.Animation
   protected T field_24;
   N next;

   AnimationList() {
   }

   protected AnimationList(T v, long t) {
      this.field_24 = v;
      this.field_23 = t;
   }

   protected abstract N makeEntry(T var1, long var2);

   public final long getTime() {
      return this.field_23;
   }

   public final T getAnimation() {
      return this.field_24;
   }

   final boolean isEmpty() {
      return this.next == null;
   }

   final void clear() {
      this.next = null;
   }

   final void insert(T v, long t) {
      this.insert(this.makeEntry(v, t));
   }

   final void insert(N entry) {
      AnimationList prev;
      AnimationList cur;
      for(prev = this; (cur = prev.next) != null && entry.field_23 >= cur.field_23; prev = cur) {
      }

      entry.next = cur;
      prev.next = entry;
   }

   final void append(T v, long t) {
      this.append(this.makeEntry(v, t));
   }

   final void append(N entry) {
      AnimationList prev;
      AnimationList cur;
      for(prev = this; (cur = prev.next) != null; prev = cur) {
      }

      prev.next = entry;
   }

   final void prepend(N entry) {
      entry.next = this.next;
      this.next = entry;
   }

   final N find(T v) {
      for(N cur = this.next; cur != null; cur = cur.next) {
         if (cur.field_24 == v) {
            return cur;
         }
      }

      return null;
   }

   final N remove(T v) {
      AnimationList cur;
      for(AnimationList<?, N> prev = this; (cur = prev.next) != null; prev = cur) {
         if (cur.field_24 == v) {
            prev.next = cur.next;
            cur.next = null;
            return cur;
         }
      }

      return null;
   }

   public final Iterator<N> iterator() {
      return this.iterator((Animation)null);
   }

   final Iterator<N> iterator(Animation runCheck) {
      return new LinkIter(this, runCheck);
   }

   final Iterable<T> animations(final Animation runCheck) {
      return new Iterable<T>() {
         public Iterator<T> iterator() {
            return new AnimIter(AnimationList.this, runCheck);
         }
      };
   }

   abstract static class BaseIter<NN extends AnimationList<?, NN>> {
      AnimationList<?, NN> prev;
      AnimationList<?, NN> cur;
      Animation runCheck;

      public BaseIter(AnimationList<?, NN> head, Animation runCheck) {
         this.cur = head;
         this.runCheck = runCheck;
      }

      public boolean hasNext() {
         return this.cur.next != null;
      }

      public NN nextLink() {
         this.prev = this.cur;
         NN ret = this.cur.next;
         this.cur = ret;
         return ret;
      }

      public void remove() {
         if (this.prev == null) {
            throw new IllegalStateException("no element to remove");
         } else if (this.runCheck != null && this.runCheck.isRunning()) {
            throw new UnsupportedOperationException("cannot call remove while animation is running");
         } else {
            this.prev.next = this.cur.next;
            this.cur.next = null;
            this.cur = this.prev;
            this.prev = null;
         }
      }
   }

   static final class AnimIter<TT extends Animation, NN extends AnimationList<TT, NN>> extends BaseIter<NN> implements Iterator<TT> {
      public AnimIter(AnimationList<?, NN> head, Animation runCheck) {
         super(head, runCheck);
      }

      public TT next() {
         return super.nextLink().getAnimation();
      }
   }

   static final class LinkIter<NN extends AnimationList<?, NN>> extends BaseIter<NN> implements Iterator<NN> {
      public LinkIter(AnimationList<?, NN> head, Animation runCheck) {
         super(head, runCheck);
      }

      public NN next() {
         return super.nextLink();
      }
   }
}
