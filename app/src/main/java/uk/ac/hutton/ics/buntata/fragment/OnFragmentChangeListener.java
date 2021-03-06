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

import android.view.*;

/**
 * The {@link OnFragmentChangeListener} can be used to get notified when a fragment should be changed.
 *
 * @author Sebastian Raubach
 */
public interface OnFragmentChangeListener
{
	void onFragmentChange(View transitionRoot, View title, int datasourceId, int parentId, int mediumId);
}