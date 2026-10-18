# WonderQuest Security Specification (Phase 0 TDD)

## 1. Data Invariants
1. **Strict Multi-Tenant Ownership**: A `ChildProfile` document at `/users/{userId}` can only be read, created, updated, or deleted when `request.auth != null` and `request.auth.uid == userId` and `incoming().userId == request.auth.uid`.
2. **Subcollection Ownership & Parent Gate**: A `GameSessionRecord` at `/users/{userId}/sessions/{sessionId}` can only be accessed or created by `request.auth.uid == userId` with `incoming().userId == request.auth.uid`.
3. **Immutable Audit Fields**: `userId` and `createdAt` can never be mutated on update (`incoming().userId == existing().userId && incoming().createdAt == existing().createdAt`).
4. **Temporal Integrity**: `createdAt` and `updatedAt` must be valid Firestore `timestamp` values with `<= request.time`.
5. **Bounded Strings, Lists, and Enums**:
   - `childName`: string, 1..40 chars
   - `ageGroup`: string in `['4-6', '7-9', '10-13']`
   - `selectedCharacter`: string in `['sparky', 'luna', 'robobit', 'sunny']`
   - `languageCode`: string in `['en', 'es', 'fr', 'hi']`
   - `parentPin`: string matching `'^[0-9]{4}$'`
   - `unlockedBadges`: list of size `<= 20`
   - `gameType`: string in `['math_safari', 'pattern_portal', 'number_bridge', 'logic_matrix']`
   - `starsEarned`: int in `1..3`

## 2. The Dirty Dozen Payloads
1. **Unauthenticated Profile Read**: `unauthDb.collection("users").doc("alice").get()` -> REJECT
2. **Cross-User Profile Read**: `bobDb.collection("users").doc("alice").get()` -> REJECT
3. **Identity Spoofing on Create**: `aliceDb.collection("users").doc("alice").set({ userId: "bob", ... })` -> REJECT
4. **Shadow/Ghost Field Injection on Profile**: Adding `isAdmin: true` to `/users/alice` -> REJECT via `hasOnly`
5. **Oversized String Poisoning**: `childName` with 500 characters -> REJECT
6. **Invalid Enum Value**: `ageGroup: "99-100"` -> REJECT
7. **Invalid Parent PIN Format**: `parentPin: "abcd"` or `"12345"` -> REJECT
8. **Unbounded Badge Array**: `unlockedBadges` with 25 items -> REJECT
9. **Mutating Immutable `createdAt` on Update**: Changing `createdAt` during profile update -> REJECT
10. **Cross-User Session List**: `bobDb.collection("users/alice/sessions").get()` -> REJECT
11. **Out-of-Range Stars in Session**: `starsEarned: 99` in `/users/alice/sessions/s1` -> REJECT
12. **Client Timestamp Skew/Epoch Int**: Passing epoch number `1700000000` for `createdAt` instead of `Timestamp` -> REJECT
