// Verify Chat Messages and Reactions in MongoDB
// Run this within Mongosh (MongoDB Shell) against the 'chat_messages' database.

// 1. Switch to the target database
db = db.getSiblingDB('chat_messages');

// 2. Count total messages in the database
print("Total messages persisted: " + db.messages.countDocuments());

// 3. View the 10 most recent messages
print("Recent messages:");
printjson(db.messages.find().sort({ createdAt: -1 }).limit(10).toArray());

// 4. Find messages in a specific chat (replace Chat UUID placeholder)
// var targetChatId = "YOUR_CHAT_UUID_HERE";
// printjson(db.messages.find({ chatId: targetChatId }).sort({ sequenceNo: 1 }).toArray());

// 5. Verify Reactions on messages
print("Messages containing reactions:");
printjson(db.messages.find({ "reactions.0": { $exists: true } }).toArray());

// 6. Find soft-deleted messages
print("Soft-deleted messages:");
printjson(db.messages.find({ isDeleted: true }).toArray());

// 7. Verify Message Search Indices are present
print("Indexes on messages collection:");
printjson(db.messages.getIndexes());
