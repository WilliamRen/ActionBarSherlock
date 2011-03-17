/*
 * Copyright 2011 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jakewharton.android.actionbarsherlock;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

//NOTE: Unqualified references to Activity in this file are to the inner-class!

/**
 * <p>Helper for implementing the action bar design pattern across all versions
 * of Android.</p>
 * 
 * <p>This class will automatically use the native
 * {@link android.app.ActionBar} implementation on Android 3.0 or later. For
 * previous versions which do not include a native action bar, an optional
 * custom handler can be provided to initialize a third-party action bar
 * implementation of your choice.</p>
 * 
 * <p>Further interaction with these action bars can be handled through
 * extending the default handler in an activity as an inner-classes. The
 * classes will allow for overriding various methods to handle the creation
 * of and interaction with each type of action bar.</p>
 * 
 * <p>Examples of third-party implementations can be found in the
 * <code>samples</code> folder of this repository, or by visiting the
 * <a href="https://github.com/JakeWharton/ActionBarSherlock/">GitHub project
 * page</a>.</p>
 * 
 * @author Jake Wharton <jakewharton@gmail.com>
 * @verion 2.0.0
 */
public final class ActionBarSherlock {
	/**
	 * <p>Whether or not the current classloader has access to Android's native
	 * {@link android.app.ActionBar} class.</p>
	 * 
	 * <p>This can be used anywhere in your application to determine whether
	 * the native {@link android.app.ActionBar} or a custom action bar will
	 * be displayed and act accordingly.</p>
	 */
	public static final boolean HAS_NATIVE_ACTION_BAR;
	
	static {
		boolean hasNativeActionBar = false;
		try {
			Class.forName("android.app.ActionBar");
			hasNativeActionBar = true;
		} catch (NoClassDefFoundError e) {
		} catch (ClassNotFoundException e) {
		} finally {
			HAS_NATIVE_ACTION_BAR = hasNativeActionBar;
		}
	}
	
	
	
	/**
	 * Whether or not this instance has been attached to the
	 * {@link android.app.Activity} yet.
	 */
	private boolean mAttached;
	
	/**
	 * The {@link android.app.Activity} on which we are binding.
	 */
	private final android.app.Activity mActivity;
	
	/**
	 * A persisted instance to forward to the implementing onCreate method.
	 */
	private Bundle mSavedInstanceState;
	
	/**
	 * Resource ID of the layout to use for the activitiy's content.
	 */
	private Integer mLayoutResourceId;
	
	/**
	 * View instance to use for the activity's content.
	 */
	private View mView;
	
	/**
	 * Title to automatically set on whichever type of action bar is selected.
	 */
	private CharSequence mTitle;
	
	/**
	 * Resource ID of the menu to inflate to the action bar.
	 */
	private Integer mMenuResourceId;
	
	/**
	 * Whether or not home should be displayed as an "up" affordance.
	 */
	private boolean mHomeAsUpEnabled;
	
	/**
	 * The class which will handle the native action bar.
	 */
	private Class<? extends NativeActionBarHandler> mNativeHandler;
	
	/**
	 * The class which will handle a custom action bar.
	 */
	private Class<? extends ActionBarHandler<?>> mCustomHandler;
	
	

	/**
	 * Create a new instance of the class and associate it to an activity.
	 * 
	 * @param activity Activity instance.
	 * @return ActionBarSherlock instance for builder pattern.
	 */
	public static ActionBarSherlock from(android.app.Activity activity) {
		return new ActionBarSherlock(activity);
	}
	
	
	
	/**
	 * Internal-only constructor to initialize this class for the builder
	 * pattern. Implementing activities should use the
	 * {@link from(android.app.Activity)} method to create a new instance.
	 * 
	 * @param activity Activity on which to bind.
	 */
	private ActionBarSherlock(android.app.Activity activity) {
		this.mAttached = false;
		this.mActivity = activity;
	}
	
	
	
	/**
	 * Include a saved state to pass to the appropriate handler's onCreate
	 * method.
	 * 
	 * @param savedInstanceState Saved instance.
	 * @return Current instance for builder pattern.
	 */
	public ActionBarSherlock with(Bundle savedInstanceState) {
		assert this.mAttached == false;
		assert this.mSavedInstanceState == null;
		
		this.mSavedInstanceState = savedInstanceState;
		return this;
	}
	
	/**
	 * Layout resource to use for the activity's content.
	 * 
	 * @param layoutResource Layout resource.
	 * @return Current instance for builder pattern.
	 */
	public ActionBarSherlock layout(int layoutResource) {
		assert this.mAttached == false;
		assert this.mLayoutResourceId == null;
		assert this.mView == null;
		
		this.mLayoutResourceId = layoutResource;
		return this;
	}
	
	/**
	 * View to use for the activity's content.
	 * 
	 * @param view Content view instance.
	 * @return Current instance for builder pattern.
	 */
	public ActionBarSherlock layout(View view) {
		assert this.mAttached == false;
		assert this.mLayoutResourceId == null;
		assert this.mView == null;
		
		this.mView = view;
		
		return this;
	}
	
	/**
	 * Initial string resource to use for setting the title of the action bar.
	 * 
	 * @param stringResourceId String resource ID.
	 * @return Current instance for builder pattern.
	 */
	public ActionBarSherlock title(int stringResourceId) {
		assert this.mAttached == false;
		return this.title(this.mActivity.getResources().getString(stringResourceId));
	}
	
	/**
	 * String to use for setting the title of the action bar.
	 * 
	 * @param title Title string.
	 * @return Current instance for builder pattern.
	 */
	public ActionBarSherlock title(CharSequence title) {
		assert this.mAttached == false;
		assert this.mTitle == null;
		
		this.mTitle = title;
		return this;
	}
	
	/**
	 * Resource ID of a menu to inflate as buttons onto the action bar. This
	 * will fall back to 
	 * 
	 * @param menuResourceId Resource ID for menu XML.
	 * @return Current instance for builder pattern.
	 */
	public ActionBarSherlock menu(int menuResourceId) {
		assert this.mAttached == false;
		assert this.mMenuResourceId == null;
		
		this.mMenuResourceId = menuResourceId;
		return this;
	}
	
	/**
	 * Set home should be displayed as an "up" affordance.
	 * 
	 * @param enabled Whether or not this is enabled.
	 * @return Current instance for builder pattern.
	 */
	public ActionBarSherlock homeAsUp(boolean enabled) {
		this.mHomeAsUpEnabled = enabled;
		return this;
	}
	
	/**
	 * Class to use for handling the native action bar creation.
	 * 
	 * @param handler Class which extends {@link NativeActionBarHandler}. If
	 *                you are doing no native handling directly you may omit
	 *                this declaration
	 * @return Current instance for builder pattern.
	 */
	public ActionBarSherlock handleNative(Class<? extends NativeActionBarHandler> handler) {
		assert this.mAttached == false;
		assert this.mNativeHandler == null;
		
		this.mNativeHandler = handler;
		return this;
	}
	
	/**
	 * Class to use for handling the custom action bar creation.
	 * 
	 * @param handler Class which extends {@link ActionBarHandler<?>}. If you
	 *                do not want an action bar on devices which do not have
	 *                the native version you may omit this for a normal
	 *                activity.
	 * @return Current instance for builder pattern.
	 */
	public ActionBarSherlock handleCustom(Class<? extends ActionBarHandler<?>> handler) {
		assert this.mAttached == false;
		assert this.mCustomHandler == null;
		
		this.mCustomHandler = handler;
		return this;
	}
	
	/**
	 * Perform the attachment to the activity and execute the appropriate
	 * onCreate callback to a handler.
	 */
	public void attach() {
		assert this.mAttached == false;
		assert (this.mLayoutResourceId != null)
			|| (this.mView != null);
		
		if (this.mNativeHandler == null) {
			this.mNativeHandler = NativeActionBarHandler.class;
		}
		
		ActionBarHandler<?> handler;
		try {
			if (HAS_NATIVE_ACTION_BAR) {
				handler = this.mNativeHandler.newInstance();
			} else if (this.mCustomHandler != null) {
				handler = this.mCustomHandler.newInstance();
			} else {
				//No custom handler so pass the view directly to the activity
				if (this.mLayoutResourceId != null) {
					this.mActivity.setContentView(this.mLayoutResourceId);
				} else {
					this.mActivity.setContentView(this.mView);
				}
				return;
			}
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
		handler.setActivity(this.mActivity);
		
		if (this.mLayoutResourceId != null) {
			handler.setLayout(this.mLayoutResourceId);
		} else {
			handler.setLayout(this.mView);
		}
		
		if ((this.mActivity instanceof ActionBarSherlock.Activity) && (this.mMenuResourceId != null)) {
			((ActionBarSherlock.Activity)this.mActivity).setActionBarMenu(this.mMenuResourceId, handler);
		}
		
		if (this.mTitle != null) {
			handler.setTitle(this.mTitle);
		}
		
		handler.setHomeAsUpEnabled(this.mHomeAsUpEnabled);
		
		handler.onCreate(this.mSavedInstanceState);
	}
	
	
	/**
	 * Base class for handling an action bar that has been created by a
	 * {@link ActionBarSherlock} attachment.
	 *
	 * @param <T> Action bar class.
	 */
	public static abstract class ActionBarHandler<T> {
		private android.app.Activity mActivity;
		private T mActionBar;

		/**
		 * Get the activity to which the action bar is bound.
		 * 
		 * @return Activity instance.
		 */
		public final android.app.Activity getActivity() {
			return this.mActivity;
		}
		
		/**
		 * <p>Set the activity to which the action bar is bound.</p>
		 * 
		 * <p><em>This should only be called internally within
		 * {@link ActionBarSherlock}</em></p>
		 * 
		 * @param activity Activity instance.
		 */
		private void setActivity(android.app.Activity activity) {
			this.mActivity = activity;
		}
		
		/**
		 * Get the action bar instance.
		 * 
		 * @return Action bar instance.
		 */
		public T getActionBar() {
			return this.mActionBar;
		}
		
		/**
		 * <p>Set the action bar instance.</p>
		 * 
		 * <p><em>This should only be called internally within
		 * {@link ActionBarSherlock}</em></p>
		 * 
		 * @param actionBar Action bar instance.
		 */
		private void setActionBar(T actionBar) {
			this.mActionBar = actionBar;
		}
		
		/**
		 * <p>Initialize the activity's layout.</p>
		 * 
		 *  <p>This method will assign the current action bar instance with the
		 *  return value of a call to the extending class' initialize
		 *  method.</p>
		 * 
		 * @param layoutResourceId Layout resource ID.
		 */
		private void setLayout(int layoutResourceId) {
			this.setActionBar(this.initialize(layoutResourceId));
		}
		
		/**
		 * <p>Initialize the activity's layout.</p>
		 * 
		 *  <p>This method will assign the current action bar instance with the
		 *  return value of a call to the extending class' initialize
		 *  method.</p>
		 *  
		 * @param view View instance.
		 */
		private void setLayout(View view) {
			this.setActionBar(this.initialize(view));
		}
		
		/**
		 * Initialize the activity's layout using a layout resource.
		 * 
		 * @param layoutResourceId Layout resource ID.
		 * @return Action bar instance.
		 */
		public abstract T initialize(int layoutResourceId);
		
		/**
		 * Initialize the activity's layout using an existing view.
		 * 
		 * @param view View instance.
		 * @return Action bar instance.
		 */
		public abstract T initialize(View view);
		
		/**
		 * Set the title of the action bar.
		 * 
		 * @param title Title string.
		 */
		public abstract void setTitle(CharSequence title);
		
		/**
		 * <p>Callback method for when the attachment is complete and the
		 * handler may perform any additional actions in setting up the action
		 * bar.</p>
		 * 
		 * <p>This is implemented as an empty method so that overriding in
		 * a custom action bar implementation handler and/or an extension of
		 * a custom handler within an activity is optional.</p>
		 * 
		 * @param savedInstanceState Saved activity instance.
		 */
		public void onCreate(Bundle savedInstanceState) {
			//Grumble, grumble... OVERRIDE ME!
		}
		
		/**
		 * This should be called when an action bar button is clicked. This
		 * method will automatically pass the call on to the parent activity.
		 * 
		 * @param item Clicked MenuItem.
		 */
		protected void clicked(MenuItem item) {
			this.getActivity().onOptionsItemSelected(item);
		}
		
		/**
		 * Set whether or not home should be displayed as an "up" affordance.
		 * 
		 * @param enabled Whether or not this is enabled.
		 */
		public void setHomeAsUpEnabled(boolean enabled) {
			//Grumble, grumble... OVERRIDE ME!
		}
	}
	
	
	/**
	 * Minimal handler for Android's native {@link android.app.ActionBar}.
	 */
	public static class NativeActionBarHandler extends ActionBarHandler<android.app.ActionBar> {
		@Override
		public android.app.ActionBar initialize(int layoutResourceId) {
			//For native action bars assigning a layout is all that is required
			this.getActivity().setContentView(layoutResourceId);
			
			return this.getActivity().getActionBar();
		}
		
		@Override
		public android.app.ActionBar initialize(View view) {
			//For native action bars assigning a layout is all that is required
			this.getActivity().setContentView(view);
			
			return this.getActivity().getActionBar();
		}

		@Override
		public void setTitle(CharSequence title) {
			this.getActionBar().setTitle(title);
		}
		
		@Override
		public void setHomeAsUpEnabled(boolean enabled) {
			this.getActionBar().setDisplayHomeAsUpEnabled(enabled);
		}
	}
	
	
	/**
	 * Interface which denotes a third-party action bar handler implementation
	 * supports populating the action bar from an inflated XML menu.
	 */
	public static interface ActionBarMenuHandler {
		/**
		 * Populate the action bar with items from the inflated XML menu.
		 * 
		 * @param menu Inflated XML menu.
		 */
		public void inflateMenu(ActionBarMenu menu);
	}
	
	
	/**
	 * Special activity wrapper which will allow for unifying common
	 * functionality via the {@link ActionBarSherlock} activity API.
	 */
	public static abstract class Activity extends android.app.Activity {
		/**
		 * Resource ID of menu XML.
		 */
		private Integer mMenuResourceId;
		
		/**
		 * Whether or not the handler support inflation of XML menus.
		 */
		private boolean mHasMenuHandler;
		
		/**
		 * Set the menu XML resource ID. This will also attempt inflation to
		 * a third-party action bar if it supports XML menus.
		 * 
		 * @param menuResourceId Resource ID of menu XML.
		 * @param handler Action bar handler.
		 */
		public void setActionBarMenu(int menuResourceId, ActionBarHandler<?> handler) {
			this.mMenuResourceId = menuResourceId;
			
			if (!ActionBarSherlock.HAS_NATIVE_ACTION_BAR) {
				if (handler.getActionBar() instanceof Menu) {
					//If the action bar implements menu, inflate directly
					this.getMenuInflater().inflate(this.mMenuResourceId, (Menu)handler.getActionBar());
					
					this.mHasMenuHandler = true;
				} else if (handler instanceof ActionBarMenuHandler) {
					//Has menu, not native, handler handles menu
					ActionBarMenuHandler menuHandler = (ActionBarMenuHandler)handler;
					
					ActionBarMenu menu = new ActionBarMenu(handler.getActivity());
					this.getMenuInflater().inflate(this.mMenuResourceId, menu);
					
					//Delegate to the handler for addition to the action bar
					menuHandler.inflateMenu(menu);
					
					this.mHasMenuHandler = true;
				}
			}
		}

		@Override
		public final boolean onCreateOptionsMenu(Menu menu) {
			if ((this.mMenuResourceId != null) && (ActionBarSherlock.HAS_NATIVE_ACTION_BAR || !this.mHasMenuHandler)) {
				//Inflate to native action bar or native menu if no handler
				this.getMenuInflater().inflate(this.mMenuResourceId, menu);
				return true;
			} else {
				//No applicable inflation targets
				return false;
			}
		}
	}
}