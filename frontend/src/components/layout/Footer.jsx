export default function Footer() {
  return (
    <footer className="bg-white border-t border-gray-200 py-6">
      <div className="max-w-7xl mx-auto px-4 text-center text-gray-500">
        <p className="text-senior-base">
          &copy; {new Date().getFullYear()} Sahasathi. All rights reserved.
        </p>
      </div>
    </footer>
  );
}
