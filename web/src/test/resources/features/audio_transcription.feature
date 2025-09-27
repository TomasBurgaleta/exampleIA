Feature: Audio Transcription Web Interface
  As a user of the ExampleIA application
  I want to upload WAV files through the web interface
  So that I can get audio transcriptions using Azure Speech Services

  Background:
    Given the ExampleIA application is running
    And the web interface is accessible

  Scenario: Successfully load the web interface
    When I navigate to the home page
    Then I should see the audio transcription form
    And the file upload button should be visible
    And the transcribe button should be disabled initially

  Scenario: Upload a valid WAV file through the web interface
    Given I am on the audio transcription page
    When I select a valid WAV audio file
    And I click the transcribe button
    Then I should see the loading indicator
    And I should receive a transcription result
    And the result should contain the object ID
    And the result should contain the transcribed text
    And the result should show the audio file size

  Scenario: Handle invalid file upload
    Given I am on the audio transcription page
    When I try to upload a non-WAV file
    Then I should see an error message about unsupported file type
    And the transcribe button should remain disabled

  Scenario: Handle empty file upload
    Given I am on the audio transcription page
    When I try to upload an empty file
    Then I should see an error message about empty file
    And the transcribe button should remain disabled

  Scenario: Health check endpoint is accessible
    When I access the health check endpoint
    Then I should receive a successful response
    And the response should indicate the service is UP
    And the response should show the service name