package com.sun.scenario.animation;

import java.util.Iterator;

final class Schedule extends AnimationList<Animation, Schedule> implements ScheduledAnimation {
   public Schedule() {
   }

   private Schedule(Animation anim, long tsched) {
      super(anim, tsched);
   }

   protected Schedule makeEntry(Animation anim, long tsched) {
      return new Schedule(anim, tsched);
   }

   public final void scheduleRelativeTo(long when) {
      for(Schedule cur = (Schedule)this.next; cur != null; cur = (Schedule)cur.next) {
         cur.v.startAt(when + cur.t);
      }

   }

   void transferUpTo(long trel, long tend, RunQueue runq) {
      for(Schedule cur = (Schedule)this.next; cur != null; cur = (Schedule)cur.next) {
         long tstart = trel + cur.t;
         if (tstart <= tend) {
            cur.v.scheduleTo(tstart, tend, runq);
         }
      }

   }

   public final Iterable<Schedule> entries(final Animation runCheck) {
      return new Iterable<Schedule>() {
         public Iterator<Schedule> iterator() {
            return Schedule.this.iterator(runCheck);
         }
      };
   }
}
