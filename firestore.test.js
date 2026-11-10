const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
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

test("unauthenticated user cannot read or write user profile", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.collection("users").doc("user_alice").get());
  await assertFails(
    unauthedDb.collection("users").doc("user_alice").set({
      userId: "user_alice",
      email: "alice@example.com",
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});

test("authenticated user can create and read their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext("user_alice").firestore();
  const userDoc = aliceDb.collection("users").doc("user_alice");

  await assertSucceeds(
    userDoc.set({
      userId: "user_alice",
      email: "alice@example.com",
      displayName: "Alice",
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );

  await assertSucceeds(userDoc.get());
});

test("user cannot read or write another user's profile", async () => {
  // Alice creates her profile
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc("user_alice").set({
      userId: "user_alice",
      email: "alice@example.com",
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const bobDb = testEnv.authenticatedContext("user_bob").firestore();
  await assertFails(bobDb.collection("users").doc("user_alice").get());
  await assertFails(
    bobDb.collection("users").doc("user_alice").update({
      displayName: "Hacked by Bob",
      updatedAt: new Date()
    })
  );
});

test("user can create, read, and delete their own design project", async () => {
  const aliceDb = testEnv.authenticatedContext("user_alice").firestore();
  const projectDoc = aliceDb.collection("users").doc("user_alice").collection("projects").doc("proj_1");

  await assertSucceeds(
    projectDoc.set({
      userId: "user_alice",
      title: "Neon Cyberpunk",
      canvasWidth: 1080,
      canvasHeight: 1080,
      presetName: "Square 1:1",
      layerCount: 5,
      layersJson: "[]",
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );

  await assertSucceeds(projectDoc.get());

  await assertSucceeds(
    projectDoc.update({
      title: "Updated Title",
      updatedAt: new Date()
    })
  );

  await assertSucceeds(projectDoc.delete());
});

test("user cannot create or read another user's design project", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc("user_alice").collection("projects").doc("proj_1").set({
      userId: "user_alice",
      title: "Secret Project",
      canvasWidth: 1080,
      canvasHeight: 1080,
      presetName: "Square 1:1",
      layerCount: 1,
      layersJson: "[]",
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const bobDb = testEnv.authenticatedContext("user_bob").firestore();
  await assertFails(
    bobDb.collection("users").doc("user_alice").collection("projects").doc("proj_1").get()
  );
  await assertFails(
    bobDb.collection("users").doc("user_alice").collection("projects").doc("proj_2").set({
      userId: "user_bob",
      title: "Bob Infiltration",
      canvasWidth: 1080,
      canvasHeight: 1080,
      presetName: "Square 1:1",
      layerCount: 1,
      layersJson: "[]",
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});
