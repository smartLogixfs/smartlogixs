import { Router, Request, Response, NextFunction } from "express";
import { validate } from "../middleware/validate.js";
import { checkoutSchema } from "../schemas/checkout.js";
import { checkout } from "../services/checkoutService.js";

const router = Router();

router.post("/checkout", validate(checkoutSchema), async (req: Request, res: Response, next: NextFunction) => {
  try {
    const result = await checkout(req.body);
    res.status(201).json(result);
  } catch (err) {
    next(err);
  }
});

export default router;
