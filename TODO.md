# StegoCam Message Extraction Fix Plan

## Problem Analysis
The extracted message shows garbled characters ('?T??`') before the actual message ('heyo'), indicating an issue with the message extraction or decryption process.

## Steps to Fix

1. **Analyze StegoEngine extraction logic**
   - Review the `extractMessage` method
   - Fix end marker detection to properly identify message boundaries
   - Ensure proper bit-to-byte conversion

2. **Test the fix**
   - Create/run unit tests to validate extraction works correctly
   - Verify that extracted messages match original messages

3. **Optional improvements**
   - Add better error handling for decryption failures
   - Improve logging for debugging purposes

## Current Status
- [x] Analyzed all relevant source files
- [x] Created implementation plan
- [ ] Implement StegoEngine fixes
- [ ] Test the implementation
- [ ] Verify message extraction works correctly
