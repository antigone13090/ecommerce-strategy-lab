function q(sel){ return document.querySelector(sel); }
function qa(sel){ return Array.from(document.querySelectorAll(sel)); }

function addRow(){
  const tbody = q("#itemsTable tbody");
  const tr = document.createElement("tr");
  tr.innerHTML = `
    <td><input class="sku" value="A1"/></td>
    <td><input class="unitPrice" value="10.00"/></td>
    <td><input class="qty" value="1"/></td>
    <td><button class="danger" type="button" onclick="removeRow(this)">X</button></td>
  `;
  tbody.appendChild(tr);
}

function removeRow(btn){
  const tr = btn.closest("tr");
  const tbody = q("#itemsTable tbody");
  if (tbody.children.length <= 1) return;
  tr.remove();
}

function buildPayload(){
  const items = qa("#itemsTable tbody tr").map(tr => {
    const sku = tr.querySelector(".sku").value.trim();
    const unitPrice = tr.querySelector(".unitPrice").value.trim();
    const qty = parseInt(tr.querySelector(".qty").value.trim(), 10);
    return { sku, unitPrice, qty };
  });

  return {
    items,
    loyaltyTier: q("#loyaltyTier").value.trim(),
    promoCode: q("#promoCode").value.trim(),
    country: q("#country").value.trim()
  };
}

function setText(id, txt){ q(id).textContent = txt; }

async function quote(){
  setText("#errors", "—");

  const payload = buildPayload();
  setText("#payloadPreview", JSON.stringify(payload, null, 2));

  try{
    const res = await fetch("/api/checkout/quote", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    const text = await res.text();
    if(!res.ok){
      setText("#result", "—");
      setText("#errors", text);
      return;
    }

    const json = JSON.parse(text);
    setText("#result", JSON.stringify(json, null, 2));

    // KPIs
    setText("#kpiSubtotal", json.subtotal ?? "—");
    setText("#kpiAfter", json.totalAfterDiscounts ?? "—");
    setText("#kpiShip", json.shippingCost ?? "—");
    setText("#kpiFinal", json.finalTotal ?? "—");
  } catch(e){
    setText("#result", "—");
    setText("#errors", String(e));
  }
}
