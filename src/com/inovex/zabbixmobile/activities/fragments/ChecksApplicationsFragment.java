package com.inovex.zabbixmobile.activities.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.ChecksApplicationsPagerAdapter;
import com.inovex.zabbixmobile.listeners.OnChecksItemSelectedListener;
import com.inovex.zabbixmobile.listeners.OnItemsLoadedListener;
import com.inovex.zabbixmobile.model.Host;
import com.viewpagerindicator.TitlePageIndicator;

public class ChecksApplicationsFragment extends BaseServiceConnectedFragment
		implements OnItemsLoadedListener {

	public static final String TAG = ChecksApplicationsFragment.class
			.getSimpleName();

	private int mHostPosition = 0;
	private long mHostId;
	private Host mHost;
	private boolean mApplicationsLoadingSpinnerVisible = true;
	private boolean mItemsLoadingSpinnerVisible = true;

	private static final String ARG_HOST_POSITION = "arg_host_position";
	private static final String ARG_HOST_ID = "arg_host_id";
	private static final String ARG_APPLICATIONS_SPINNER_VISIBLE = "arg_applications_spinner_visible";
	private static final String ARG_ITEMS_SPINNER_VISIBLE = "arg_items_spinner_visible";

	private TextView mTitleView;

	protected ViewPager mDetailsPager;
	protected TitlePageIndicator mDetailsPageIndicator;
	protected ChecksApplicationsPagerAdapter mDetailsPagerAdapter;

	private OnChecksItemSelectedListener mCallbackMain;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mCallbackMain = (OnChecksItemSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnChecksItemSelectedListener.");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mHostPosition = savedInstanceState.getInt(ARG_HOST_POSITION, 0);
			mHostId = savedInstanceState.getLong(ARG_HOST_ID, 0);
			mApplicationsLoadingSpinnerVisible = savedInstanceState.getBoolean(
					ARG_APPLICATIONS_SPINNER_VISIBLE, false);
			mItemsLoadingSpinnerVisible = savedInstanceState.getBoolean(
					ARG_ITEMS_SPINNER_VISIBLE, false);
		}
		// setRetainInstance(true);
		Log.d(TAG, "Host position: " + mHostPosition + "; id: " + mHostId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_checks_details, container);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mTitleView = (TextView) view.findViewById(R.id.checks_title);
		if (mApplicationsLoadingSpinnerVisible)
			showApplicationsLoadingSpinner();
		// if (mItemsLoadingSpinnerVisible)
		// showItemsLoadingSpinner();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(ARG_HOST_POSITION, mHostPosition);
		outState.putLong(ARG_HOST_ID, mHostId);
		outState.putBoolean(ARG_APPLICATIONS_SPINNER_VISIBLE,
				mApplicationsLoadingSpinnerVisible);
		outState.putBoolean(ARG_ITEMS_SPINNER_VISIBLE,
				mItemsLoadingSpinnerVisible);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		super.onServiceConnected(name, service);

		setupDetailsViewPager();
		if (mHost != null)
			mTitleView.setText("Host: " + mHost.getName());

	}

	public void setHost(Host h) {
		this.mHost = h;
		if (mHost != null && getView() != null)
			mTitleView.setText("Host: " + mHost.getName());
	}

	/**
	 * Causes a redraw of the page indicator. This needs to be called when the
	 * adapter's contents are updated.
	 */
	public void redrawPageIndicator() {
		if (mDetailsPageIndicator != null) {
			// mDetailsPageIndicator.invalidate();
			if (mDetailsPagerAdapter.getCount() > 0)
				mDetailsPageIndicator.onPageSelected(0);
		}
	}

	/**
	 * Performs the setup of the view pager used to swipe between details pages.
	 */
	protected void setupDetailsViewPager() {
		Log.d(TAG, "setupViewPager");

		retrievePagerAdapter();
		if (mDetailsPagerAdapter != null) {
			mDetailsPagerAdapter.setFragmentManager(getChildFragmentManager());

			// initialize the view pager
			mDetailsPager = (ViewPager) getView().findViewById(
					R.id.checks_view_pager);
			mDetailsPager.setAdapter(mDetailsPagerAdapter);

			// Initialize the page indicator
			mDetailsPageIndicator = (TitlePageIndicator) getView()
					.findViewById(R.id.checks_page_indicator);
			mDetailsPageIndicator.setViewPager(mDetailsPager);
			mDetailsPageIndicator.setCurrentItem(mHostPosition);
			mDetailsPageIndicator
					.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

						@Override
						public void onPageScrollStateChanged(int arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onPageScrolled(int arg0, float arg1,
								int arg2) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onPageSelected(int position) {
							Log.d(TAG, "detail page selected: " + position);

							mDetailsPagerAdapter.setCurrentPosition(position);
							mDetailsPager.setCurrentItem(position);
							mDetailsPageIndicator.setCurrentItem(position);

							// mDetailsPagerAdapter.getCurrentPage().selectItem(0);
							mCallbackMain.onApplicationSelected(position);

							showItemsLoadingSpinner();
							mZabbixDataService.loadItemsByApplicationId(
									mDetailsPagerAdapter.getCurrentItem()
											.getId(),
									ChecksApplicationsFragment.this);

							// propagate page change only if there actually was
							// a
							// change -> prevent infinite propagation
							// mDetailsPagerAdapter.setCurrentPosition(position);
							// if (position != mPosition)
							// mCallbackMain.onListItemSelected(position,
							// mDetailsPagerAdapter.getItemId(position));
						}
					});
		}
	}

	protected void retrievePagerAdapter() {
		mDetailsPagerAdapter = mZabbixDataService
				.getChecksApplicationsPagerAdapter();
	}

	/**
	 * Shows a loading spinner instead of this page's list view.
	 */
	public void showApplicationsLoadingSpinner() {
		mApplicationsLoadingSpinnerVisible = true;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.applications_progress_layout);
			if (progressLayout != null)
				progressLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Dismisses the loading spinner view.
	 * 
	 * If the view has not yet been created, the status is saved and when the
	 * view is created, the spinner will not be shown at all.
	 */
	public void dismissApplicationsLoadingSpinner() {
		mApplicationsLoadingSpinnerVisible = false;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.applications_progress_layout);
			if (progressLayout != null) {
				progressLayout.setVisibility(View.GONE);
			}
		}
		// for (ChecksDetailsPage p : mDetailsPagerAdapter.getPages()) {
		// p.dismissLoadingSpinner();
		// }
	}

	@Override
	public void onDetach() {
		super.onDetach();
		// This is a fix for the issue with the child fragment manager;
		// described here:
		// http://stackoverflow.com/questions/15207305/getting-the-error-java-lang-illegalstateexception-activity-has-been-destroyed
		// and here: https://code.google.com/p/android/issues/detail?id=42601
		// If the fragment manager is not set to null, there will be issues when
		// the activity is destroyed and there are pending transactions
		mDetailsPagerAdapter.setFragmentManager(null);
	}

	/**
	 * Shows a loading spinner instead of this page's list view.
	 */
	public void showItemsLoadingSpinner() {
		mItemsLoadingSpinnerVisible = true;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.items_progress_layout);
			if (progressLayout != null)
				progressLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Dismisses the loading spinner view.
	 * 
	 * If the view has not yet been created, the status is saved and when the
	 * view is created, the spinner will not be shown at all.
	 */
	public void dismissItemsLoadingSpinner() {
		mItemsLoadingSpinnerVisible = false;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.items_progress_layout);
			if (progressLayout != null) {
				progressLayout.setVisibility(View.GONE);
			}
		}

	}

	@Override
	public void onItemsLoaded() {
		dismissItemsLoadingSpinner();
	}

	public void selectItem(int position) {
		if (mDetailsPagerAdapter == null
				|| mDetailsPagerAdapter.getCount() == 0)
			return;
		ChecksApplicationsPage currentPage = (ChecksApplicationsPage) mDetailsPagerAdapter
				.instantiateItem(mDetailsPager, mDetailsPager.getCurrentItem());
		Log.d(TAG, "selectItem(" + position + ")");
		currentPage.selectItem(position);
	}

}