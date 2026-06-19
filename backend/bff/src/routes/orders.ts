import { Router, Request, Response, NextFunction } from "express";
import { pedidoFull } from "../services/orderComposerService.js";

const router = Router();

router.get("/orders/:id/full", async (req: Request, res: Response, next: NextFunction) => {
  try {
    const data = await pedidoFull(req.params.id);
    res.json(data);
  } catch (err) {
    next(err);
  }
});

export default router;
