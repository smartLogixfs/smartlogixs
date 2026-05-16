import { Router } from "express";
import { dashboard } from "../services/dashboardService.js";

const router = Router();

router.get("/dashboard", async (_req, res, next) => {
  try {
    const data = await dashboard();
    res.json(data);
  } catch (err) {
    next(err);
  }
});

export default router;
