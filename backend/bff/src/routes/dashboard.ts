import { Router, Request, Response, NextFunction } from "express";
import { dashboard } from "../services/dashboardService.js";

const router = Router();

router.get("/dashboard", async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const data = await dashboard();
    res.json(data);
  } catch (err) {
    next(err);
  }
});

export default router;
