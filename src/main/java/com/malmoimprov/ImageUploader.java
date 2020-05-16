/*
 * Copyright 2018 jonatan.jonsson
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.malmoimprov;

import java.io.OutputStream;
import java.nio.channels.Channels;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Acl.Role;
import com.google.cloud.storage.Acl.User;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobField;
import com.google.cloud.storage.Storage.BlobGetOption;
import com.google.cloud.storage.StorageOptions;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Image;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

class ImageUploader implements Receiver, SucceededListener
{
	private static final long serialVersionUID = 1L;

	private Image image;

	public ImageUploader(Image image)
	{
		this.image = image;
	}

	@Override
	public OutputStream receiveUpload(String filename, String mimeType)
	{
		Storage storage = StorageOptions.getDefaultInstance().getService();
		BlobId blobId = BlobId.of("malmo-improv.appspot.com", filename);
		WriteChannel writer = storage.writer(BlobInfo.newBuilder(blobId).setContentType(mimeType).build());
		return Channels.newOutputStream(writer);
	}

	@Override
	public void uploadSucceeded(SucceededEvent event)
	{
		Storage storage = StorageOptions.getDefaultInstance().getService();
		BlobId blobId = BlobId.of("malmo-improv.appspot.com", event.getFilename());
		storage.createAcl(blobId, Acl.of(User.ofAllUsers(), Role.READER));
		Blob blob = storage.get(blobId, BlobGetOption.fields(BlobField.MEDIA_LINK));
		image.setSource(new ExternalResource(blob.getMediaLink()));
	}

	public static void main(String[] args)
	{
		System.out.println(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).withZone(ZoneId.of("GMT"))
				.format(LocalDateTime.now()));
	}
}
