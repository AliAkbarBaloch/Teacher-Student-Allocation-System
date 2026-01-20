// Setup file for Vitest + Testing Library
import "@testing-library/jest-dom/vitest";

// JSDOM polyfills for Radix UI (Popper/Tooltip) and similar libs
// eslint-disable-next-line @typescript-eslint/no-explicit-any
if (!(globalThis as any).ResizeObserver) {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  (globalThis as any).ResizeObserver = class ResizeObserver {
    observe() {}
    unobserve() {}
    disconnect() {}
  };
}

// Radix Select relies on Pointer Events APIs that JSDOM doesn't fully implement.
// Provide no-op pointer capture methods to avoid crashes in unit tests.
if (!Element.prototype.hasPointerCapture) {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  Element.prototype.hasPointerCapture = function (_pointerId: number) {
    return false;
  };
}
if (!Element.prototype.setPointerCapture) {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  Element.prototype.setPointerCapture = function (_pointerId: number) {};
}
if (!Element.prototype.releasePointerCapture) {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  Element.prototype.releasePointerCapture = function (_pointerId: number) {};
}

if (!Element.prototype.scrollIntoView) {
  Element.prototype.scrollIntoView = function () {};
}
