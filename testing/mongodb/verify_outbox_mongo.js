// Verify Outbox Messages in MongoDB (message-service outbox collection)
// Run this within Mongosh (MongoDB Shell) against the 'chat_messages' database.

// 1. Switch to the target database
db = db.getSiblingDB('chat_messages');

// 2. Count total documents in the outbox collection
print("Total outbox documents: " + db.outbox_messages.countDocuments());

// 3. Count documents by their current status
printjson(db.outbox_messages.aggregate([
    { $group: { _id: "$status", count: { $sum: 1 } } }
]).toArray());

// 4. View all pending outbox events (events waiting to be published to Kafka)
print("Pending outbox events:");
printjson(db.outbox_messages.find({ status: "PENDING" }).sort({ createdAt: 1 }).limit(10).toArray());

// 5. View the last 5 published events
print("Recent published outbox events:");
printjson(db.outbox_messages.find({ status: "PUBLISHED" }).sort({ processedAt: -1 }).limit(5).toArray());

// 6. View events with delivery failures or high attempt counts
print("Failed or high-attempt outbox events:");
printjson(db.outbox_messages.find({ $or: [ { status: "FAILED" }, { attempts: { $gt: 3 } } ] }).toArray());
