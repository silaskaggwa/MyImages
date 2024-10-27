# MyImages App
This app provides an API to upload an images, analyzes them for object detection and returns the enhanced content.

## Running Application
### Requirements
- Docker
### Steps
1. Install Docker
2. Clone repository
3. Navigate to the myimages directory
4. Run ```docker-compose up```. Application will start running on http://localhost:8080 while MySQL database will be running on localhost port 3306.

## API Specification
Swagger: http://localhost:8080/swagger-ui/index.html

```GET /images```
- Returns HTTP 200 OK with a JSON response containing all image metadata. 

```GET /images?objects="dog,cat"```
- Returns a HTTP 200 OK with a JSON response body containing only images that have
the detected objects specified in the query parameter.

```GET /images/{imageId}```
- Returns HTTP 200 OK with a JSON response containing image metadata for the
specified image.

```POST /images```
- Send a JSON request body including an image file or URL, an optional label for the
image, and an optional field to enable object detection.
- Returns a HTTP 200 OK with a JSON response body including the image data, its label
(generate one if the user did not provide it), its identifier provided by the persistent data
store, and any objects detected (if object detection was enabled).
 