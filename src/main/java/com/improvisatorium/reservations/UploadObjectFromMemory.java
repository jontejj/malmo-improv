/* Copyright 2024 jonatanjonsson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.improvisatorium.reservations;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Acl.Role;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.ImmutableList;

public class UploadObjectFromMemory
{
	public static void uploadObjectFromMemory(String projectId, String bucketName, String objectName, String contentType, byte[] content)
	{

		Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
		BlobId blobId = BlobId.of(bucketName, objectName);
		Acl publicAccess = Acl.of(Acl.User.ofAllUsers(), Role.READER);
		Acl jonatan = Acl.of(new Acl.User("JonteJJ@gmail.com"), Role.OWNER);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).setAcl(ImmutableList.of(publicAccess, jonatan)).build();

		// Optional: set a generation-match precondition to enable automatic retries, avoid potential
		// race conditions and data corruptions. The request returns a 412 error if the preconditions are not met.
		Storage.BlobTargetOption precondition;
		if(storage.get(bucketName, objectName) == null)
		{
			// For a target object that does not yet exist, set the DoesNotExist precondition.
			// This will cause the request to fail if the object is created before the request runs.
			precondition = Storage.BlobTargetOption.doesNotExist();
		}
		else
		{
			// If the destination already exists in your bucket, instead set a generation-match
			// precondition. This will cause the request to fail if the existing object's generation
			// changes before the request runs.
			precondition = Storage.BlobTargetOption.generationMatch(storage.get(bucketName, objectName).getGeneration());
		}
		storage.create(blobInfo, content, precondition);

		System.out.println("Object " + objectName + " uploaded to bucket " + bucketName + " with type " + contentType + " and contents " + content);
	}
}
