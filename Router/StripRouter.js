import express from 'express';
import { Router } from 'express';
import StripeContorller from '../Controller/StripeController.js';
const StripRouter=express.Router();
const StripeControl=new StripeContorller();
StripRouter.post('/',(req,res)=>{
StripeControl.adddata(req,res);
})
export {StripRouter};