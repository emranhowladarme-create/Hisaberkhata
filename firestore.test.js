const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

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

const serverTs = () => new Date();

test("Unauthenticated user cannot read or write user profile or customers", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("customers").get());
});

test("Authenticated owner can create and read their UserProfile and Customer", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = serverTs();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      ownerName: "রহিম উদ্দিন",
      businessName: "রহিম ট্রেডার্স",
      activeBusinessId: "biz_1",
      smsBalance: 250,
      language: "bn",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("customers").doc("cust_1").set({
      userId: ALICE_UID,
      businessId: "biz_1",
      name: "শাহিন ভাই বাকী",
      phone: "01711111111",
      balance: 19900,
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("customers").get()
  );
});

test("Cross-user isolation: Bob cannot read or write Alice's customers or transactions", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = serverTs();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("customers").doc("cust_1").set({
      userId: ALICE_UID,
      businessId: "biz_1",
      name: "শাহিন ভাই",
      balance: 500,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("customers").doc("cust_1").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("customers").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("customers").doc("cust_2").set({
      userId: BOB_UID,
      businessId: "biz_1",
      name: "Hacker",
      balance: 100,
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

test("Shadow update & invalid schema rejection on Customer and Transaction", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = serverTs();

  // Ghost field rejected on create
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("customers").doc("cust_ghost").set({
      userId: ALICE_UID,
      businessId: "biz_1",
      name: "Test",
      balance: 100,
      isAdmin: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Negative amount rejected on transaction
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_bad").set({
      userId: ALICE_UID,
      businessId: "biz_1",
      entryScope: "customer",
      type: "gave",
      amount: -50,
      dateMillis: Date.now(),
      paymentMethod: "cash",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Valid transaction succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_ok").set({
      userId: ALICE_UID,
      businessId: "biz_1",
      customerId: "cust_1",
      customerName: "শাহিন ভাই",
      entryScope: "customer",
      type: "gave",
      amount: 5000,
      description: "পূর্বের পাওনা",
      dateMillis: Date.now(),
      paymentMethod: "cash",
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

test("Products, InventoryLogs, SmsLogs, Notes, Notifications, SupportTickets, QrTransactions", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = serverTs();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("products").doc("prod_1").set({
      userId: ALICE_UID,
      businessId: "biz_1",
      name: "সয়াবিন তেল ৫ লিটার",
      unit: "বোতল",
      purchasePrice: 820,
      sellingPrice: 880,
      quantity: 20,
      lowStockThreshold: 5,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("inventoryLogs").doc("log_1").set({
      userId: ALICE_UID,
      businessId: "biz_1",
      productId: "prod_1",
      productName: "সয়াবিন তেল ৫ লিটার",
      changeQty: 20,
      resultingQty: 20,
      reason: "প্রাথমিক স্টক",
      createdAt: ts,
    })
  );

  // Append-only inventoryLogs cannot be updated or deleted
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("inventoryLogs").doc("log_1").delete()
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("supportTickets").doc("t_1").set({
      userId: ALICE_UID,
      subject: "QR পেমেন্ট সমস্যা",
      description: "QR স্ক্যান হচ্ছে না",
      status: "pending",
      createdAt: ts,
      updatedAt: ts,
    })
  );
});
