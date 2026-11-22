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

test("Unauthenticated users cannot read or write any VideoChat Pro collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("conversations").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("calls").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Hacker",
      country: "USA",
      diamonds: 10000,
      beautyFilterEnabled: true,
      hdVideoEnabled: true,
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("User profile create, partial update, and security bounds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  // Create valid profile matching Kotlin UserProfileRepository payload
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Aarav Sharma",
      country: "India",
      diamonds: 10000,
      beautyFilterEnabled: true,
      hdVideoEnabled: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Owner get succeeds; list users fails; cross-user get/update fails
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  await assertFails(aliceDb.collection("users").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      diamonds: 99999,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Partial update of diamonds & daily reward timestamp succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      diamonds: 20000,
      lastRewardClaimedAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject ghost field, negative diamonds, future timestamp, or createdAt mutation
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      diamonds: -50,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      updatedAt: new Date(Date.now() + 60000),
    })
  );
});

test("Conversations and immutable chat messages follow ownership and schema rules", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  const convRef = aliceDb.collection("users").doc(ALICE_UID).collection("conversations").doc("priya");
  await assertSucceeds(
    convRef.set({
      userId: ALICE_UID,
      creatorId: "priya",
      creatorName: "Priya",
      creatorCountry: "India",
      lastMessage: "Hey! Ready for a video call?",
      unreadCount: 1,
      favorite: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Subcollection list succeeds for owner, fails for other user
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("conversations").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("conversations").get());

  // Reject `@DocumentId` id field inside written document
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("conversations").doc("anaya").set({
      id: "anaya",
      userId: ALICE_UID,
      creatorId: "anaya",
      creatorName: "Anaya",
      creatorCountry: "Pakistan",
      lastMessage: "Hi!",
      unreadCount: 0,
      favorite: false,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Add immutable message
  const msgRef = convRef.collection("messages").doc("msg_1");
  await assertSucceeds(
    msgRef.set({
      userId: ALICE_UID,
      creatorId: "priya",
      sender: "USER",
      text: "Sent you a diamond gift!",
      giftDiamonds: 500,
      createdAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(convRef.collection("messages").get());
  await assertFails(msgRef.update({ text: "Tampered" }));
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("conversations").doc("priya").collection("messages").get());
});

test("Call records enforce valid status transitions and terminal locking", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  const callRef = aliceDb.collection("users").doc(ALICE_UID).collection("calls").doc("call_1");
  await assertSucceeds(
    callRef.set({
      userId: ALICE_UID,
      creatorId: "rohit",
      creatorName: "Rohit",
      creatorCountry: "India",
      callType: "VIDEO",
      status: "CONNECTED",
      durationSeconds: 0,
      diamondsSpent: 0,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Complete call
  await assertSucceeds(
    callRef.update({
      status: "COMPLETED",
      durationSeconds: 184,
      diamondsSpent: 500,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Terminal state locked: subsequent update must fail
  await assertFails(
    callRef.update({
      durationSeconds: 999,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("calls").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("calls").get());
});
