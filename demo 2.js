const { MongoClient } = require('mongodb');

// MongoDB Atlas connection string
const url = 'mongodb+srv://joshuasamyx:YdYWNJfS7wnbfVlo@cluster0.0cryh.mongodb.net/Main?retryWrites=true&w=majority&appName=Cluster0';

const dbName = 'Main';  // Use your database name from Atlas
const collectionName = 'User Data';  // Collection name where you want to insert data

// Function to connect to MongoDB
async function connectToDB() {
    const client = new MongoClient(url);
    try {
        await client.connect();  // Connect to MongoDB
        console.log('Connected to MongoDB Atlas');
        const db = client.db(dbName);  // Access the database
        const collection = db.collection(collectionName);  // Access the collection

        // New document to insert
        const newUser = {
            _id: {
                full_name: "LeAnh Ly",
                phone: "714-874-6088",
                email: "leanhtly@gmail.com",
                gender: "female",
                dob: new Date("2002-09-09"),  // Ensure correct date format
                account: {
                    username: "leanhtly",
                    password: "hello123",
                    passcode: "123"
                }
            }
        };

        // Insert the new user document into the collection
        const insertResult = await collection.insertOne(newUser);
        console.log('Inserted document:', insertResult);

        // Retrieve all users from the collection
        const users = await collection.find({}).toArray();  // Retrieve all documents in the collection
        console.log('All users:', users);

    } catch (err) {
        console.error('Error:', err);
    }
}

// Call the function to connect and perform operations
connectToDB();
