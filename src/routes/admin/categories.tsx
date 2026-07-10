import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useCallback, useEffect, useState } from "react";
import { ArrowLeft, Plus, Pencil, Trash2, FolderOpen, X, CheckCircle } from "lucide-react";
import { useAuth } from "@/contexts/auth-context";
import {
  createCategory,
  deleteCategory,
  fetchCategories,
  updateCategory,
  type Category,
} from "@/lib/api";

export const Route = createFileRoute("/admin/categories")({
  head: () => ({ meta: [{ title: "Categories — RASICK-EX" }] }),
  component: CategoryManagement,
});

const COLORS = [
  "from-blue-500 to-indigo-600",
  "from-pink-500 to-rose-700",
  "from-amber-500 to-orange-600",
  "from-emerald-500 to-teal-700",
];

function CategoryManagement() {
  const { isAdmin } = useAuth();
  const navigate = useNavigate();
  const [categories, setCategories] = useState<Category[]>([]);
  const [showAdd, setShowAdd] = useState(false);
  const [newName, setNewName] = useState("");
  const [editId, setEditId] = useState<string | null>(null);
  const [editName, setEditName] = useState("");
  const [toast, setToast] = useState("");

  const loadCategories = useCallback(async () => {
    try {
      const data = await fetchCategories();
      setCategories(data);
    } catch {
      setCategories([]);
    }
  }, []);

  useEffect(() => {
    if (!isAdmin) navigate({ to: "/login-selection" });
  }, [isAdmin, navigate]);

  useEffect(() => {
    if (isAdmin) loadCategories();
  }, [isAdmin, loadCategories]);

  const showToast = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(""), 2000);
  };

  const handleAdd = async () => {
    if (!newName.trim()) return;
    try {
      await createCategory(newName.trim(), COLORS[categories.length % COLORS.length]);
      setNewName("");
      setShowAdd(false);
      showToast("Category created successfully");
      await loadCategories();
    } catch (err) {
      window.alert(err instanceof Error ? err.message : "Failed to create category");
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteCategory(id);
      showToast("Category deleted");
      await loadCategories();
    } catch (err) {
      window.alert(err instanceof Error ? err.message : "Failed to delete category");
    }
  };

  const handleEdit = async (id: string) => {
    if (!editName.trim()) return;
    try {
      await updateCategory(id, { name: editName.trim() });
      setEditId(null);
      setEditName("");
      showToast("Category updated");
      await loadCategories();
    } catch (err) {
      window.alert(err instanceof Error ? err.message : "Failed to update category");
    }
  };

  if (!isAdmin) return null;

  return (
    <main className="relative min-h-screen bg-background pb-10">
      <div
        className="pointer-events-none absolute inset-x-0 top-0 h-64"
        style={{ background: "var(--gradient-glow)", opacity: 0.45 }}
      />

      {/* Header */}
      <header className="relative z-10 flex items-center justify-between px-5 pt-10">
        <button
          onClick={() => navigate({ to: "/admin/dashboard" })}
          className="flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors"
        >
          <ArrowLeft className="h-4 w-4" /> Dashboard
        </button>
        <button
          onClick={() => { setShowAdd(true); setNewName(""); }}
          className="flex items-center gap-1.5 rounded-full bg-gradient-primary px-4 py-2 text-xs font-semibold text-primary-foreground shadow-glow"
        >
          <Plus className="h-3.5 w-3.5" /> Create Category
        </button>
      </header>

      <div className="relative z-10 flex flex-col items-center px-5 pt-6">
        <div className="grid h-14 w-14 place-items-center rounded-2xl bg-secondary">
          <FolderOpen className="h-7 w-7 text-primary" />
        </div>
        <h1 className="mt-4 text-xl font-bold">Category Management</h1>
        <p className="text-xs text-muted-foreground">{categories.length} categories</p>
      </div>

      {/* Toast */}
      {toast && (
        <div className="relative z-10 mx-5 mt-4 flex items-center gap-2 rounded-xl border border-emerald-500/40 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-400">
          <CheckCircle className="h-4 w-4" /> {toast}
        </div>
      )}

      {/* Add new category form */}
      {showAdd && (
        <div className="relative z-10 mx-5 mt-6 glass rounded-2xl p-4">
          <div className="mb-3 flex items-center justify-between">
            <p className="text-sm font-semibold">New Category</p>
            <button onClick={() => setShowAdd(false)} className="text-muted-foreground hover:text-foreground">
              <X className="h-4 w-4" />
            </button>
          </div>
          <div className="flex gap-2">
            <div className="flex-1 glass flex items-center rounded-xl border border-input px-3 py-2.5 focus-within:border-primary/60">
              <input
                type="text"
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleAdd()}
                placeholder="Category name"
                className="w-full bg-transparent text-sm focus:outline-none"
                autoFocus
              />
            </div>
            <button
              onClick={handleAdd}
              disabled={!newName.trim()}
              className="rounded-full bg-gradient-primary px-4 text-sm font-semibold text-primary-foreground shadow-glow disabled:opacity-40"
            >
              Add
            </button>
          </div>
        </div>
      )}

      {/* Categories list */}
      <section className="relative z-10 mt-6 flex flex-col gap-3 px-5">
        {categories.map((cat) => (
          <div key={cat.id} className="glass rounded-2xl overflow-hidden">
            {editId === cat.id ? (
              /* Inline edit */
              <div className="flex items-center gap-2 p-4">
                <div className={`h-2 w-8 rounded-full bg-gradient-to-r ${cat.color}`} />
                <div className="flex-1 flex items-center rounded-xl border border-input bg-secondary/40 px-3 py-2 focus-within:border-primary/60">
                  <input
                    type="text"
                    value={editName}
                    onChange={(e) => setEditName(e.target.value)}
                    onKeyDown={(e) => e.key === "Enter" && handleEdit(cat.id)}
                    className="w-full bg-transparent text-sm focus:outline-none"
                    autoFocus
                  />
                </div>
                <button onClick={() => handleEdit(cat.id)} className="text-xs text-primary font-semibold">Save</button>
                <button onClick={() => setEditId(null)} className="text-muted-foreground"><X className="h-4 w-4" /></button>
              </div>
            ) : (
              <div className="flex items-center gap-4 p-4">
                <div className={`h-12 w-12 shrink-0 rounded-xl bg-gradient-to-br ${cat.color} grid place-items-center`}>
                  <FolderOpen className="h-5 w-5 text-white" />
                </div>
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-semibold">{cat.name}</p>
                  <p className="text-xs text-muted-foreground">{cat.count} songs</p>
                </div>
                <div className="flex items-center gap-1.5">
                  <button
                    onClick={() => { setEditId(cat.id); setEditName(cat.name); }}
                    className="grid h-8 w-8 place-items-center rounded-lg text-muted-foreground hover:bg-secondary/80 hover:text-amber-400 transition-colors"
                  >
                    <Pencil className="h-3.5 w-3.5" />
                  </button>
                  <button
                    onClick={() => handleDelete(cat.id)}
                    className="grid h-8 w-8 place-items-center rounded-lg text-muted-foreground hover:bg-secondary/80 hover:text-destructive transition-colors"
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </button>
                </div>
              </div>
            )}
          </div>
        ))}
      </section>
    </main>
  );
}
