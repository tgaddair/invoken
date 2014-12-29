package com.eldritch.invoken.actor.ai;

public interface StateValidator {
	public void update(float delta);
	
	boolean isValid();
	
	// Default validator, always valid.  Singleton.
	public static class BasicValidator implements StateValidator {
		@Override
		public void update(float delta) {
		}

		@Override
		public boolean isValid() {
			return true;
		}
		
		public static BasicValidator getInstance() {
			return BasicValidatorHolder.INSTANCE;
		}
		
		private BasicValidator() {}
		
		private static class BasicValidatorHolder {
	        private static final BasicValidator INSTANCE = new BasicValidator();
	    }
	}
	
	// Timed validator, expires after some amount of time has elapsed.
	public static class TimedValidator implements StateValidator {
		private final float duration;
		private float elapsed = 0;
		
		public TimedValidator(float duration) {
			this.duration = duration;
		}
		
		@Override
		public void update(float delta) {
			elapsed += delta;
		}

		@Override
		public boolean isValid() {
			return elapsed <= duration;
		}
	}
}
