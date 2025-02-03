const functions = require('firebase-functions');
const express = require('express');
const bodyparser = require('body-parser');
const {Server, Socket} = require('socket.io');

const io = new Server({
    cors: true,
});

const app = express();

app.use(bodyparser.json());

const emailToSocketMapping = new Map();
const socketToEmailMapping = new Map();


io.on('connection', (Socket) =>{
    console.log("New Connection");
    Socket.on('join-room', (data) => {
        const { roomId, emailId } = data;
        console.log("User", emailId, "Joined room", roomId);
        emailToSocketMapping.set(emailId, Socket.id);
        socketToEmailMapping.set(Socket.id, emailId);
        Socket.join(roomId);
        Socket.emit('joined-room', { roomId });
        Socket.broadcast.to(roomId).emit('user-joined', { emailId });
    });

    Socket.on("call-user", (data) => {
        const {emailId, offer} = data;
        const fromEmail = socketToEmailMapping.get(Socket.id);
        const socketId = emailToSocketMapping.get(emailId);
        Socket.to(socketId).emit("incoming-call", {from : fromEmail, offer});
    });

    Socket.on("call-accepted", (data) => {
        const {emailId, ans } = data;
        const socketId = emailToSocketMapping.get(emailId);
        Socket.to(socketId).emit("call-accepted", { ans });
    });
 });

app.listen(8000,() => console.log('http server running at port 8000'));
io.listen(8001);

exports.app = functions.https.onRequest(app);