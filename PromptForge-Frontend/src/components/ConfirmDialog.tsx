import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { AlertTriangle, Trash, Info } from "lucide-react";
import { cn } from "@/lib/utils";

export type ConfirmVariant = "danger" | "warning" | "info";

interface ConfirmDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description: string;
  confirmText?: string;
  cancelText?: string;
  variant?: ConfirmVariant;
  onConfirm: () => void;
  onCancel?: () => void;
  loading?: boolean;
}

const variantConfig: Record<
  ConfirmVariant,
  {
    icon: typeof Trash;
    iconBg: string;
    iconColor: string;
    actionClass: string;
  }
> = {
  danger: {
    icon: Trash,
    iconBg: "bg-red-500/10",
    iconColor: "text-red-500",
    actionClass:
      "bg-red-600 text-white hover:bg-red-700 focus:ring-red-600/20 border-0",
  },
  warning: {
    icon: AlertTriangle,
    iconBg: "bg-amber-500/10",
    iconColor: "text-amber-500",
    actionClass:
      "bg-amber-600 text-white hover:bg-amber-700 focus:ring-amber-600/20 border-0",
  },
  info: {
    icon: Info,
    iconBg: "bg-primary/10",
    iconColor: "text-primary",
    actionClass: "",
  },
};

export function ConfirmDialog({
  open,
  onOpenChange,
  title,
  description,
  confirmText = "Confirm",
  cancelText = "Cancel",
  variant = "danger",
  onConfirm,
  onCancel,
  loading = false,
}: ConfirmDialogProps) {
  const config = variantConfig[variant];
  const Icon = config.icon;

  const handleCancel = () => {
    onCancel?.();
    onOpenChange(false);
  };

  const handleConfirm = () => {
    onConfirm();
  };

  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent className="max-w-[420px] p-0 overflow-hidden border-border/50 bg-background/95 backdrop-blur-xl shadow-2xl">
        {/* Top accent bar */}
        <div
          className={cn(
            "h-1 w-full",
            variant === "danger" && "bg-gradient-to-r from-red-500 to-rose-600",
            variant === "warning" && "bg-gradient-to-r from-amber-500 to-orange-500",
            variant === "info" && "bg-gradient-to-r from-primary to-blue-600"
          )}
        />

        <div className="px-6 pt-5 pb-2">
          <AlertDialogHeader className="flex-row items-start gap-4 space-y-0">
            {/* Icon circle */}
            <div
              className={cn(
                "shrink-0 w-11 h-11 rounded-full flex items-center justify-center ring-4 ring-background",
                config.iconBg
              )}
            >
              <Icon className={cn("w-5 h-5", config.iconColor)} />
            </div>
            <div className="flex-1 pt-0.5">
              <AlertDialogTitle className="text-base font-semibold leading-tight">
                {title}
              </AlertDialogTitle>
              <AlertDialogDescription className="mt-1.5 text-sm leading-relaxed text-muted-foreground">
                {description}
              </AlertDialogDescription>
            </div>
          </AlertDialogHeader>
        </div>

        <AlertDialogFooter className="px-6 pb-5 pt-3 flex-row gap-2.5 sm:gap-2.5">
          <AlertDialogCancel
            onClick={handleCancel}
            disabled={loading}
            className="flex-1 sm:flex-none mt-0 sm:mt-0 h-9 text-sm font-medium"
          >
            {cancelText}
          </AlertDialogCancel>
          <AlertDialogAction
            onClick={handleConfirm}
            disabled={loading}
            className={cn(
              "flex-1 sm:flex-none h-9 text-sm font-medium transition-all",
              config.actionClass
            )}
          >
            {loading ? (
              <span className="flex items-center gap-2">
                <span className="w-3.5 h-3.5 border-2 border-current/30 border-t-current rounded-full animate-spin" />
                Processing…
              </span>
            ) : (
              confirmText
            )}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
