/*
 * Copyright 2016 Information & Computational Sciences, The James Hutton Institute
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

package uk.ac.hutton.ics.buntata.fragment;

import android.database.sqlite.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v4.content.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;

import com.heinrichreimersoftware.materialintro.app.*;

import java.util.*;

import butterknife.*;
import jhi.buntata.resource.*;
import uk.ac.hutton.ics.buntata.*;
import uk.ac.hutton.ics.buntata.activity.*;
import uk.ac.hutton.ics.buntata.adapter.*;
import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.database.manager.*;
import uk.ac.hutton.ics.buntata.service.*;
import uk.ac.hutton.ics.buntata.util.*;

/**
 * The {@link DatasourceFragment} shows all the {@link BuntataDatasource}s that are available locally and the ones available online (if connection
 * available).
 *
 * @author Sebastian Raubach
 */
public class DatasourceFragment extends Fragment
{
	@BindView(R.id.datasource_text)
	TextView     text;
	@BindView(R.id.datasource_recycler_view)
	RecyclerView recyclerView;
	@BindView(R.id.datasource_network_warning)
	TextView     networkWarning;
	private DatasourceAdapter adapter;

	private Unbinder unbinder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_datasource, container, false);

		unbinder = ButterKnife.bind(this, view);

		if (getActivity() instanceof IntroActivity)
			text.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.white));

		recyclerView.setHasFixedSize(false);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setItemAnimator(null);

		int valueInPixels = (int) getResources().getDimension(R.dimen.activity_vertical_margin) / 2;
		recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, valueInPixels, valueInPixels, valueInPixels));

		return view;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();

		unbinder.unbind();
	}

	@Override
	public void onResume()
	{
		super.onResume();

		/* If  this is part of the DatasourceActivity, then load the content here */
		if (getActivity() instanceof DatasourceActivity)
			updateStatus();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);

		/* If this is part of the IntroActivity, then only load the content once this is visible. Otherwise it'll load this fragment already on the previous page. */
		if (isVisibleToUser && getActivity() instanceof IntroActivity)
			updateStatus();
	}

	private void updateStatus()
	{
		if (!NetworkUtils.hasNetworkConnection(getActivity()))
		{
			networkWarning.setVisibility(View.VISIBLE);
		}
		else
		{
			networkWarning.setVisibility(View.GONE);
			requestData();
		}
	}

	private void requestData()
	{
		/* Get the local data sources */
		List<BuntataDatasource> local;

		try
		{
			local = new DatasourceManager(getActivity(), -1).getAll();
		}
		catch (SQLiteException e)
		{

			local = new ArrayList<>();
		}

		final List<BuntataDatasource> localList = local;
		/* Keep track of their status (installed no update, installed update, not installed) */
		final List<BuntataDatasourceAdvanced> dataset = new ArrayList<>();

		final boolean cancelable = getActivity() instanceof DatasourceActivity;

		/* Set it initially */
		adapter = new DatasourceAdapter(getActivity(), recyclerView, dataset);
		recyclerView.setAdapter(adapter);

		/* Then try to get the online resources */
		DatasourceService.getAll(getActivity(), cancelable, new RemoteCallback<List<BuntataDatasource>>(getActivity())
		{
			@Override
			public void onFailure(Throwable caught)
			{
				caught.printStackTrace();

				/* If the request fails, just show the local ones as having no updates */
				for (BuntataDatasource ds : localList)
				{
					BuntataDatasourceAdvanced adv = BuntataDatasourceAdvanced.create(ds);
					adv.setState(BuntataDatasourceAdvanced.InstallState.INSTALLED_NO_UPDATE);
					dataset.add(adv);
				}

				if (dataset.size() < 1)
				{
					networkWarning.setVisibility(View.VISIBLE);
				}
				else
				{
					networkWarning.setVisibility(View.GONE);

					adapter = new DatasourceAdapter(getActivity(), recyclerView, dataset);
					recyclerView.setAdapter(adapter);
				}
			}

			@Override
			public void onSuccess(List<BuntataDatasource> result)
			{
				/* If the request succeeds, try to figure out if it's already installed locally and then check if there's an update */
				for (BuntataDatasource ds : result)
				{
					int index = localList.indexOf(ds);

					BuntataDatasourceAdvanced adv = BuntataDatasourceAdvanced.create(ds);

					/* Is installed */
					if (index != -1)
					{
						BuntataDatasource old = localList.get(index);

						boolean isNewer = DatasourceManager.isNewer(ds, old);

						if (isNewer)
							adv.setState(BuntataDatasourceAdvanced.InstallState.INSTALLED_HAS_UPDATE);
						else
							adv.setState(BuntataDatasourceAdvanced.InstallState.INSTALLED_NO_UPDATE);
					}
					/* Is not installed */
					else
					{
						adv.setState(BuntataDatasourceAdvanced.InstallState.NOT_INSTALLED);
					}

					dataset.add(adv);
				}

				/* Set whatever we got now to the adapter */
				adapter = new DatasourceAdapter(getActivity(), recyclerView, dataset);
				recyclerView.setAdapter(adapter);
			}
		});
	}
}
