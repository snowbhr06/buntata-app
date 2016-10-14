/**
 * Germinate Mobile is written and developed by Sebastian Raubach, Paul Shaw and David Marshall from the Information and Computational Sciences Group
 * at JHI Dundee. For further information contact us at germinate@hutton.ac.uk or visit our webpages at http://ics.hutton.ac.uk/germinate-scan/
 * <p/>
 * Copyright (c) 2012-2016, Information & Computational Sciences, The James Hutton Institute. All rights reserved. Use is subject to the accompanying
 * licence terms.
 */

package uk.ac.hutton.ics.knodel.service;

import android.content.Context;

import org.restlet.*;
import org.restlet.data.*;
import org.restlet.engine.application.*;
import org.restlet.resource.*;

import java.util.*;

import uk.ac.hutton.ics.knodel.util.*;

/**
 * This is the base Restlet Service class. Sub-classes can use the {@link #getResource()} and {@link #getBaseUrl(Context)} convenience methods
 * to simplify the request.
 *
 * @author Sebastian Raubach
 */
public class RestletService
{
	private static ClientResource clientResource;
	private static Client         client;
	private static Decoder        decoder;

	public static void updateResources()
	{
		if (clientResource == null)
		{
			/* Accept http and https protocols */
			List<Protocol> protocols = new ArrayList<>();
			protocols.add(Protocol.HTTP);
			protocols.add(Protocol.HTTPS);

			/* Set up the client using the protocols */
			client = new Client(protocols);

			/* Handle de-compression */
			decoder = new Decoder(client.getContext(), false, true);
			decoder.setNext(client);

			/* Set up the client resource with an empty URL */
			clientResource = new ClientResource("");
			clientResource.setNext(decoder);

			/* Accept the following encodings */
			clientResource.accept(Encoding.GZIP);
			clientResource.accept(Encoding.DEFLATE);
//			clientResource.accept(Encoding.ALL);

			/* Accept the result in any media type form */
			clientResource.accept(MediaType.ALL);
		}
	}

	/**
	 * Returns the re-usable {@link org.restlet.resource.ClientResource} that can be used to make the request.
	 * <p/>
	 * HTTP and HTTPS connections are allowed and the request allows GZIP and DEFLATE encoded responses.
	 *
	 * @return The {@link org.restlet.resource.ClientResource}
	 */
	protected static synchronized ClientResource getResource()
	{
		if (clientResource == null)
			updateResources();

		return clientResource;
	}

	/**
	 * Returns the base URL of the BRAPI instance. Will <b>ALEAYS</b> end with a tailing slash
	 *
	 * @return The base URL of the BRAPI instance. Will <b>ALEAYS</b> end with a tailing slash
	 */
	public static String getBaseUrl(Context context)
	{
		String url = PreferenceUtils.getPreference(context, PreferenceUtils.PREFS_KNODEL_SERVER_URL);
		if (StringUtils.isEmpty(url) || !url.endsWith("/"))
			url += "/";

		return url;
	}
}