import { Router } from "express";
import { pedidoFull } from "../services/orderComposerService.js";

const router = Router();

router.get("/pedidos/:id/full", async (req, res, next) => {
  try {
    const data = await pedidoFull(req.params.id);
    res.json(data);
  } catch (err) {
    next(err);
  }
});

export default router;
