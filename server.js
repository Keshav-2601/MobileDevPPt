import express from 'express';
import { StripRouter } from './Router/StripRouter.js';

const server =express();
server.use(express.json())
server.use('/create-payment-intent',StripRouter);
server.listen(3000,()=>{
    console.log("server is running");
})