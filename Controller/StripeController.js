import express from "express";
import Stripe from "stripe";
import dotenv from "dotenv";
dotenv.config();
export default class StripeContorller{
    async adddata(req,res){
        try {
            const amount=req.body.amount;
            const currency=req.body.currency;
            const stripe=Stripe(process.env.STRIPE_SECRET_KEY);
            const paymentIntent=await stripe.paymentIntents.create({
                amount,
                currency,
              });
              if(paymentIntent){
                return res.status(200).json({message:paymentIntent.client_secret});
              }
              return res.status(400).send("Error occured not successfully payment done");
        } catch (error) {
            console.log("There is some errro amoutn and currency not recieved ",error);
        }
       
    }
}