# API Testing

This directory contains API testing resources for the Uber Location Service.

## Bruno Collection

Bruno is a modern, open-source API client alternative to Postman.

### How to Import the Bruno Collection

1. **Install Bruno** (if not already installed):
   - Download from: https://www.usebruno.com/
   - Or via Homebrew: `brew install bruno`

2. **Import the Collection**:
   - Open Bruno
   - Click on **"Open Collection"** (or use `Cmd + O` on Mac / `Ctrl + O` on Windows)
   - Navigate to this directory: `api-tests/bruno-collection/`
   - Select the `bruno-collection` folder
   - Click "Open"

3. **Alternative: Direct Path**:
   - In Bruno, go to: **Collections** → **Open Collection**
   - Paste the full path:
     ```
     /Users/ggarg1/Personal/Backend Projects/Microservices-Based Ride-Hailing Platform/Uber-Location-Service/api-tests/bruno-collection
     ```

### Available Endpoints

The collection includes the following requests:

#### 1. Add Driver
- **Method**: POST
- **URL**: `http://localhost:7477/api/location/drivers`
- **Purpose**: Save a driver's location to Redis
- **Body**:
  ```json
  {
    "driverId": "driver-123",
    "latitude": 28.7041,
    "longitude": 77.1025
  }
  ```

#### 2. Search Nearby Drivers
- **Method**: POST
- **URL**: `http://localhost:7477/api/location/nearby/drivers`
- **Purpose**: Find all drivers within 5km radius of a location
- **Body**:
  ```json
  {
    "latitude": 28.7041,
    "longitude": 77.1025
  }
  ```

### Prerequisites

- Ensure the Uber Location Service is running on port `7477`
- Redis should be running on `localhost:6379`

### Running Tests

1. Start the application:
   ```bash
   ./gradlew bootRun
   ```

2. Execute requests in Bruno:
   - First, add some drivers using the "Add Driver" request
   - Then search for nearby drivers using the "Search Nearby Drivers" request

### Using Environment Variables

The collection uses environment variables for flexibility:

- `{{baseUrl}}` - Base URL of the service (default: `http://localhost:7477`)
- `{{apiPrefix}}` - API path prefix (default: `/api/location`)

**To switch environments in Bruno**:
1. Look for the environment dropdown in the top-right corner
2. Select "local" environment
3. You can create additional environments (e.g., `dev.bru`, `staging.bru`) by copying `environments/local.bru`

### Collection Structure

```
api-tests/
├── bruno-collection/
│   ├── bruno.json                    # Collection configuration
│   ├── Add Driver.bru                # Request to add driver location
│   ├── Search Nearby Drivers.bru     # Request to find nearby drivers
│   └── environments/
│       └── local.bru                 # Local environment variables
└── README.md                         # This file
```

## Shell Script Testing

For automated testing via command line, see the test script in the root directory:
- `test-location-service.sh` - Automated curl-based testing

## Notes

- Bruno stores collections as plain text files (`.bru` format) in your filesystem
- This makes it easy to version control your API tests
- No account or cloud sync required
- All requests use `application/json` content type

