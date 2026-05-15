import { Router } from "express";
import { validate } from "../middleware/validate.js";
import { checkoutSchema } from "../schemas/checkout.js";
import { checkout } from "../services/checkoutService.js";

const router = Router();

router.post("/checkout", validate(checkoutSchema), async (req, res, next) => {
  try {
    const result = await checkout(req.body);
    res.status(201).json(result);
  } catch (err) {
    next(err);
  }
});

export default router;
