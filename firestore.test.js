const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfilePayload(userId = ALICE_UID, now = new Date(Date.now() - 1000)) {
  return {
    userId,
    childName: "Mia Explorer",
    ageGroup: "7-9",
    selectedCharacter: "sparky",
    languageCode: "en",
    darkMode: false,
    parentPin: "1234",
    totalStars: 12,
    completedLevelsCount: 4,
    mathScore: 150,
    logicScore: 120,
    streakDays: 3,
    unlockedBadges: ["first_star", "math_whiz"],
    createdAt: now,
    updatedAt: now,
  };
}

function validSessionPayload(userId = ALICE_UID, now = new Date(Date.now() - 1000)) {
  return {
    userId,
    gameType: "math_safari",
    ageGroup: "7-9",
    levelNumber: 2,
    starsEarned: 3,
    correctAnswers: 5,
    totalQuestions: 5,
    durationSeconds: 45,
    createdAt: now,
    updatedAt: now,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or create profiles", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID)));
});

test("Authenticated user can create and read their own valid ChildProfile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user cannot read or overwrite another user's ChildProfile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID)));

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).set(validProfilePayload(BOB_UID)));
});

test("Shadow update with ghost field is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const initialDate = new Date(Date.now() - 2000);
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, initialDate)));
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      totalStars: 15,
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Invalid parentPin or invalid ageGroup is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const badPinPayload = { ...validProfilePayload(ALICE_UID), parentPin: "99a1" };
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).set(badPinPayload));

  const badAgePayload = { ...validProfilePayload(ALICE_UID), ageGroup: "18+" };
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).set(badAgePayload));
});

test("Authenticated user can create and list their own GameSessionRecords with userId filter", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const sessionRef = aliceDb.collection("users").doc(ALICE_UID).collection("sessions").doc("session_1");
  await assertSucceeds(sessionRef.set(validSessionPayload(ALICE_UID)));
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("sessions")
      .where("userId", "==", ALICE_UID)
      .get()
  );
});

test("Cross-user session read and list are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const sessionRef = aliceDb.collection("users").doc(ALICE_UID).collection("sessions").doc("session_1");
  await assertSucceeds(sessionRef.set(validSessionPayload(ALICE_UID)));

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("sessions").doc("session_1").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("sessions")
      .where("userId", "==", ALICE_UID)
      .get()
  );
});
