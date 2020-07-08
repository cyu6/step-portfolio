// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.lang.IllegalStateException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles comments data. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private static final String DEFAULT_INPUT_NAME = "Anonymous";
  private static final String DEFAULT_INPUT_EMAIL = "N/A";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query(Comment.getEntityKind()).addSort("timestampMillis", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    int limit = Integer.parseInt(request.getParameter("comment-limit"));
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(limit));

    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results) {
      long id = entity.getKey().getId();
      String name = (String) entity.getProperty("name");
      String email = (String) entity.getProperty("email");
      long timestampMillis = (long) entity.getProperty("timestampMillis");
      String commentInput = (String) entity.getProperty("commentInput");
      String fileUrl = (String) entity.getProperty("fileUrl");

      Comment comment = new Comment(id, name, email, timestampMillis, commentInput, fileUrl);
      comments.add(comment);
    }

    response.setContentType("application/json;");
    response.getWriter().println(new Gson().toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Ignore comment limit form submission and redirect back to blog page.
    if (request.getParameter("comment-limit") != null) {
      response.sendRedirect("/blog.html"); 
      return;
    }

    long timestampMillis = System.currentTimeMillis();
    // Get the input from the form.
    String name = getParameter(request, "name", DEFAULT_INPUT_NAME);
    String email = getParameter(request, "email", DEFAULT_INPUT_EMAIL);
    String commentInput = getParameter(request, "comment-input", "");

    // Create a new Comment and add it to the Datastore.
    Entity commentEntity = new Entity(Comment.getEntityKind());
    commentEntity.setProperty("name", name);
    commentEntity.setProperty("email", email);
    commentEntity.setProperty("commentInput", commentInput);
    commentEntity.setProperty("timestampMillis", timestampMillis);

    // Handle file upload input from form.
    getUploadedFileUrl(request, "file").ifPresent(fileUrl -> commentEntity.setProperty("fileUrl", fileUrl));
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity); 

    // Redirect back to the blog page.
    response.sendRedirect("/blog.html");
  }

  private static String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  /** Returns a URL that points to the uploaded file, or null if the user didn't upload a file. */
  private static Optional<String> getUploadedFileUrl(HttpServletRequest request, 
                                                     String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return Optional.empty();
    }

    // Make sure there is only one input.
    if (blobKeys.size() != 1) {
      throw new IllegalStateException("There should only be one file input.");
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return Optional.empty();
    }

    // We could check the validity of the file here, e.g. to make sure it's an image file
    // https://stackoverflow.com/q/10779564/873165

    // Use ImagesService to get a URL that points to the uploaded file.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);

    // To support running in Google Cloud Shell with AppEngine's devserver, we must use the relative
    // path to the image, rather than the path returned by imagesService which contains a host.
    try {
      URL url = new URL(imagesService.getServingUrl(options));
      return Optional.of(url.getPath());
    } catch (MalformedURLException e) {
      return Optional.of(imagesService.getServingUrl(options));
    }
  }
}
