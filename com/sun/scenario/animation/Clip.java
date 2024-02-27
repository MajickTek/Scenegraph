package com.sun.scenario.animation;

import java.util.ArrayList;
import java.util.Iterator;

public final class Clip extends ClipFactory {
   private ArrayList<TimingTarget> targets = new ArrayList();
   private Animation.Status desiredStatus;
   private float repeatCount;
   private RepeatBehavior repeatBehavior;
   private EndBehavior endBehavior;
   private long duration;
   private int resolution;
   private Interpolator interpolator;
   private boolean autoReverse;
   private Direction direction;
   private long offsetT;
   private long lastElapsed;
   private boolean offsetValid;
   private Schedule relBegin;
   private Schedule relEnd;
   public static final int INDEFINITE = -1;

   private void validateRepeatCount(float repeatCount) {
      if (repeatCount < 0.0F && repeatCount != -1.0F) {
         throw new IllegalArgumentException("repeatCount (" + repeatCount + ") cannot be < 0");
      }
   }

   public static Clip create(long duration, TimingTarget target) {
      return new Clip(duration, 1.0F, target);
   }

   public static Clip create(long duration, float repeatCount, TimingTarget target) {
      return new Clip(duration, repeatCount, target);
   }

   Clip(long duration, float repeatCount, TimingTarget target) {
      this.desiredStatus = Animation.Status.STOPPED;
      this.repeatCount = 1.0F;
      this.repeatBehavior = Clip.RepeatBehavior.REVERSE;
      this.endBehavior = Clip.EndBehavior.HOLD;
      this.resolution = 20;
      this.interpolator = Interpolators.getEasingInstance();
      this.validateRepeatCount(repeatCount);
      this.duration = duration;
      this.repeatCount = repeatCount;
      this.addTarget(target);
   }

   public Direction getDirection() {
      return this.direction != null ? this.direction : Clip.Direction.FORWARD;
   }

   private boolean isReverse() {
      return this.direction == Clip.Direction.REVERSE;
   }

   public boolean isAutoReverse() {
      return this.autoReverse;
   }

   public void setAutoReverse(boolean autoReverse) {
      this.throwExceptionIfRunning();
      this.autoReverse = autoReverse;
   }

   public Interpolator getInterpolator() {
      return this.interpolator;
   }

   public void setInterpolator(Interpolator interpolator) {
      this.throwExceptionIfRunning();
      this.interpolator = interpolator;
   }

   public void addTarget(TimingTarget target) {
      synchronized(this.targets) {
         if (!this.targets.contains(target)) {
            this.targets.add(target);
         }

      }
   }

   public void removeTarget(TimingTarget target) {
      synchronized(this.targets) {
         this.targets.remove(target);
      }
   }

   public int getResolution() {
      return this.resolution;
   }

   public void setResolution(int resolution) {
      if (resolution < 0) {
         throw new IllegalArgumentException("resolution must be >= 0");
      } else {
         this.throwExceptionIfRunning();
         this.resolution = resolution;
      }
   }

   public long getDuration() {
      return this.duration;
   }

   public void setDuration(long duration) {
      this.throwExceptionIfRunning();
      this.duration = duration;
   }

   public float getRepeatCount() {
      return this.repeatCount;
   }

   public void setRepeatCount(float repeatCount) {
      this.validateRepeatCount(repeatCount);
      this.throwExceptionIfRunning();
      this.repeatCount = repeatCount;
   }

   public RepeatBehavior getRepeatBehavior() {
      return this.repeatBehavior;
   }

   public void setRepeatBehavior(RepeatBehavior repeatBehavior) {
      this.throwExceptionIfRunning();
      this.repeatBehavior = repeatBehavior != null ? repeatBehavior : Clip.RepeatBehavior.REVERSE;
   }

   public EndBehavior getEndBehavior() {
      return this.endBehavior;
   }

   public void setEndBehavior(EndBehavior endBehavior) {
      this.throwExceptionIfRunning();
      this.endBehavior = endBehavior;
   }

   public void addBeginAnimation(Animation beginAnim) {
      this.addBeginAnimation(beginAnim, 0L);
   }

   public void addBeginAnimation(Animation beginAnim, long offset) {
      this.throwExceptionIfRunning();
      Schedule rB = this.getRelBegin();
      synchronized(rB) {
         rB.insert(beginAnim, offset);
      }
   }

   private synchronized Schedule getRelBegin() {
      if (this.relBegin == null) {
         this.relBegin = new Schedule();
      }

      return this.relBegin;
   }

   public Iterable<Animation> beginAnimations() {
      return this.getRelBegin().animations(this);
   }

   public Iterable<? extends ScheduledAnimation> beginEntries() {
      return this.getRelBegin().entries(this);
   }

   void scheduleBeginAnimations(long tBegin) {
      if (this.relBegin != null) {
         this.relBegin.scheduleRelativeTo(tBegin);
      }

   }

   public void removeBeginAnimation(Animation beginAnim) {
      this.throwExceptionIfRunning();
      if (this.relBegin != null) {
         synchronized(this.relBegin) {
            this.relBegin.remove(beginAnim);
         }
      }

   }

   public void addEndAnimation(Animation endAnim) {
      this.addEndAnimation(endAnim, 0L);
   }

   public void addEndAnimation(Animation endAnim, long offset) {
      this.throwExceptionIfRunning();
      Schedule rE = this.getRelEnd();
      synchronized(rE) {
         rE.insert(endAnim, offset);
      }
   }

   private synchronized Schedule getRelEnd() {
      if (this.relEnd == null) {
         this.relEnd = new Schedule();
      }

      return this.relEnd;
   }

   public Iterable<Animation> endAnimations() {
      return this.getRelEnd().animations(this);
   }

   public Iterable<? extends ScheduledAnimation> endEntries() {
      return this.getRelEnd().entries(this);
   }

   void scheduleEndAnimations(long tEnd) {
      if (this.relEnd != null) {
         this.relEnd.scheduleRelativeTo(tEnd);
      }

   }

   public void removeEndAnimation(Animation endAnim) {
      this.throwExceptionIfRunning();
      if (this.relEnd != null) {
         synchronized(this.relEnd) {
            this.relEnd.remove(endAnim);
         }
      }

   }

   void scheduleTo(long trel, long tend, RunQueue runq) {
      this.throwExceptionIfRunning();
      runq.insert(this, trel);
   }

   void runTo(long tstart, long tcur, RunQueue runq) {
      this.begin();
      if (this.relBegin != null) {
         this.relBegin.transferUpTo(tstart, tcur, runq);
      }

      this.desiredStatus = Animation.Status.RUNNING;
      Animation.Status s = this.timePulse(tcur - tstart);
      this.desiredStatus = Animation.Status.STOPPED;
      if (s == Animation.Status.STOPPED) {
         this.stop();
         if (this.relEnd != null) {
            long tend = tstart + (long)((double)this.getDuration() * (double)this.getRepeatCount());
            this.relEnd.transferUpTo(tend, tcur, runq);
         }
      }

   }

   void startAt(long when) {
      if (!this.autoReverse) {
         this.throwExceptionIfRunning();
      }

      this.desiredStatus = Animation.Status.RUNNING;
      MasterTimer.addToRunQueue(this, when);
   }

   public void start() {
      if (this.autoReverse) {
         this.offsetValid = false;
         this.direction = this.direction == null ? Clip.Direction.FORWARD : this.direction.opposite();
         if (!this.isRunning()) {
            if (this.direction != Clip.Direction.REVERSE) {
               this.offsetT = 0L;
            } else {
               long dur = this.duration;
               double rep = (double)this.repeatCount;
               if (dur != -1L && rep != -1.0) {
                  this.offsetT = (long)((double)dur * rep);
               } else {
                  this.offsetT = Long.MAX_VALUE;
               }

               this.offsetValid = true;
            }

            this.lastElapsed = 0L;
            super.start();
         }
      } else {
         super.start();
      }

   }

   public boolean isRunning() {
      if (this.desiredStatus == Animation.Status.RUNNING) {
         return true;
      } else {
         Iterator i$;
         Schedule a;
         if (this.relBegin != null) {
            i$ = this.relBegin.iterator();

            while(i$.hasNext()) {
               a = (Schedule)i$.next();
               if (a.getAnimation().isRunning()) {
                  return true;
               }
            }
         }

         if (this.relEnd != null) {
            i$ = this.relEnd.iterator();

            while(i$.hasNext()) {
               a = (Schedule)i$.next();
               if (a.getAnimation().isRunning()) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public void stop() {
      this.desiredStatus = Animation.Status.STOPPED;
      MasterTimer.stop(this);
   }

   public void cancel() {
      this.desiredStatus = Animation.Status.CANCELED;
      MasterTimer.removeFromRunQueue(this);
      Iterator i$;
      Schedule a;
      if (this.relBegin != null) {
         i$ = this.relBegin.iterator();

         while(i$.hasNext()) {
            a = (Schedule)i$.next();
            a.getAnimation().cancel();
         }
      }

      if (this.relEnd != null) {
         i$ = this.relEnd.iterator();

         while(i$.hasNext()) {
            a = (Schedule)i$.next();
            a.getAnimation().cancel();
         }
      }

   }

   public void pause() {
      boolean changedStatus = MasterTimer.pause(this);
      if (changedStatus) {
         synchronized(this.targets) {
            for(int i = 0; i < this.targets.size(); ++i) {
               TimingTarget target = (TimingTarget)this.targets.get(i);
               target.pause();
            }
         }
      }

   }

   public void resume() {
      boolean changedStatus = MasterTimer.resume(this);
      if (changedStatus) {
         synchronized(this.targets) {
            for(int i = 0; i < this.targets.size(); ++i) {
               TimingTarget target = (TimingTarget)this.targets.get(i);
               target.resume();
            }
         }
      }

   }

   Animation.Status timePulse(long totalElapsed) {
      if (this.desiredStatus != Animation.Status.RUNNING) {
         return this.desiredStatus;
      } else {
         if (this.autoReverse) {
            if (!this.offsetValid) {
               if (this.isReverse()) {
                  this.offsetT = totalElapsed + this.lastElapsed;
               } else {
                  this.offsetT = totalElapsed - this.lastElapsed;
               }

               this.offsetValid = true;
            }

            if (this.isReverse()) {
               totalElapsed = this.offsetT - totalElapsed;
            } else {
               totalElapsed -= this.offsetT;
            }
         }

         if (this.isReverse()) {
            if (totalElapsed <= 0L) {
               totalElapsed = 0L;
               this.desiredStatus = Animation.Status.STOPPED;
            }
         } else {
            long dur = this.duration;
            double rep = (double)this.repeatCount;
            if (dur != -1L && rep != -1.0) {
               long totalDur = (long)((double)dur * rep);
               if (totalElapsed >= totalDur) {
                  totalElapsed = totalDur;
                  this.desiredStatus = Animation.Status.STOPPED;
               }
            }
         }

         this.lastElapsed = totalElapsed;
         this.process(totalElapsed);
         return this.desiredStatus;
      }
   }

   private void process(long timeElapsed) {
      float fraction;
      if (this.duration == -1L) {
         fraction = 0.0F;
      } else if (this.duration == 0L) {
         fraction = 1.0F;
      } else {
         double iterationCount = (double)timeElapsed / (double)this.duration;
         if (this.repeatBehavior == Clip.RepeatBehavior.REVERSE) {
            iterationCount %= 2.0;
            if (iterationCount > 1.0) {
               iterationCount = 2.0 - iterationCount;
            }
         } else {
            iterationCount %= 1.0;
         }

         fraction = (float)iterationCount;
      }

      fraction = this.interpolator.interpolate(fraction);
      this.fireTimingEvent(fraction, timeElapsed);
   }

   private void fireTimingEvent(float fraction, long totalElapsed) {
      synchronized(this.targets) {
         for(int i = 0; i < this.targets.size(); ++i) {
            TimingTarget target = (TimingTarget)this.targets.get(i);
            target.timingEvent(fraction, totalElapsed);
         }

      }
   }

   void begin() {
      synchronized(this.targets) {
         for(int i = 0; i < this.targets.size(); ++i) {
            TimingTarget target = (TimingTarget)this.targets.get(i);
            target.begin();
         }

      }
   }

   void end() {
      float resetFraction = this.isReverse() ? 1.0F : 0.0F;
      long totalElapsed = (long)((double)this.duration * (double)this.repeatCount);
      synchronized(this.targets) {
         for(int i = 0; i < this.targets.size(); ++i) {
            TimingTarget target = (TimingTarget)this.targets.get(i);
            if (this.endBehavior == Clip.EndBehavior.RESET) {
               target.timingEvent(resetFraction, totalElapsed);
            }

            target.end();
         }

      }
   }

   public static enum RepeatBehavior {
      LOOP,
      REVERSE;
   }

   public static enum Direction {
      FORWARD,
      REVERSE;

      Direction opposite() {
         return this == FORWARD ? REVERSE : FORWARD;
      }
   }

   public static enum EndBehavior {
      HOLD,
      RESET;
   }
}
