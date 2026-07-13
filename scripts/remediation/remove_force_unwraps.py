from __future__ import annotations
from pathlib import Path
import sys

ROOT = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def write(path: str, text: str) -> None:
    (ROOT / path).write_text(text, encoding="utf-8")


def replace_once(text: str, old: str, new: str, label: str) -> str:
    if new in text:
        return text
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f"{label}: expected one old block, found {count}")
    return text.replace(old, new, 1)


path = "app/src/main/java/app/tijario/ui/screens/FormScreens.kt"
source = read(path)
source = replace_once(
    source,
    '''                                isDeletingCustomer = true
                                val res = dataViewModel.deleteCustomer(customerId!!)''',
    '''                                val resolvedCustomerId = customerId ?: return@launch
                                isDeletingCustomer = true
                                val res = dataViewModel.deleteCustomer(resolvedCustomerId)''',
    "customer delete guard",
)
source = replace_once(
    source,
    '''        if (selectedImageUri != null) {
            try {
                context.contentResolver.openInputStream(selectedImageUri!!)?.use { input ->''',
    '''        if (selectedImageUri != null) {
            try {
                val imageUri = selectedImageUri ?: return@remember null
                context.contentResolver.openInputStream(imageUri)?.use { input ->''',
    "product image preview guard",
)
source = replace_once(
    source,
    '''                                        if (selectedImageUri != null) {
                                            val dir = File(context.filesDir, "product_images")
                                            if (!dir.exists()) dir.mkdirs()
                                            val destFile = File(dir, "${product.id}.jpg")
                                            context.contentResolver.openInputStream(selectedImageUri!!)?.use { input ->
                                                destFile.outputStream().use { output ->
                                                    input.copyTo(output)
                                                }
                                            }
                                        } else if (imageDeleted) {''',
    '''                                        selectedImageUri?.let { imageUri ->
                                            val dir = File(context.filesDir, "product_images")
                                            if (!dir.exists()) dir.mkdirs()
                                            val destFile = File(dir, "${product.id}.jpg")
                                            context.contentResolver.openInputStream(imageUri)?.use { input ->
                                                destFile.outputStream().use { output ->
                                                    input.copyTo(output)
                                                }
                                            }
                                        } ?: if (imageDeleted) {''',
    "product image save guard",
)
source = replace_once(
    source,
    '''                                        logoBitmap != null -> Image(
                                            bitmap = logoBitmap!!.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )''',
    '''                                        logoBitmap != null -> logoBitmap?.let { bitmap ->
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop,
                                            )
                                        }''',
    "business logo bitmap guard",
)
write(path, source)


path = "app/src/main/java/app/tijario/ui/screens/DocumentDetailScreen.kt"
source = read(path)
source = replace_once(
    source,
    '''                document != null -> {
                    val doc = document!!''',
    '''                document != null -> document?.let { doc ->''',
    "document detail smart cast",
)
write(path, source)


path = "app/src/main/java/app/tijario/ui/screens/AuthScreens.kt"
source = read(path)
source = replace_once(
    source,
    '''                                        if (selectedLogoUri != null) {
                                            val uploadRequest = buildLogoUploadRequest(context, selectedLogoUri!!, language)''',
    '''                                        selectedLogoUri?.let { logoUri ->
                                            val uploadRequest = buildLogoUploadRequest(context, logoUri, language)''',
    "onboarding logo guard",
)
write(path, source)


path = "app/src/main/java/app/tijario/ui/screens/SettingsScreens.kt"
source = read(path)
source = replace_once(
    source,
    '''                                if (profilePicBitmap != null) {
                                    Image(
                                        bitmap = profilePicBitmap!!.asImageBitmap(),''',
    '''                                profilePicBitmap?.let { bitmap ->
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),''',
    "profile image guard",
)
source = source.replace(
    '''                                    )
                                } else {''',
    '''                                    )
                                } ?: run {''',
    1,
)
write(path, source)


path = "app/src/main/java/app/tijario/ui/screens/CoreScreens.kt"
source = read(path)
source = replace_once(
    source,
    '''                                deleteErrorMessage = null
                                val res = dataViewModel.deleteCustomer(customerToDelete!!.id!!)''',
    '''                                deleteErrorMessage = null
                                val customerId = customerToDelete?.id ?: return@launch
                                val res = dataViewModel.deleteCustomer(customerId)''',
    "customer list delete guard",
)
source = replace_once(
    source,
    '''                                            onClick = { onEditCustomer?.invoke(customer.id!!) },''',
    '''                                            onClick = { customer.id?.let { onEditCustomer?.invoke(it) } },''',
    "customer edit guard",
)
source = replace_once(
    source,
    '''                                deleteErrorMessage = null
                                val res = dataViewModel.deleteProduct(productToDelete!!.id!!)''',
    '''                                deleteErrorMessage = null
                                val productId = productToDelete?.id ?: return@launch
                                val res = dataViewModel.deleteProduct(productId)''',
    "product list delete guard",
)
source = replace_once(
    source,
    '''                                                onEditProduct?.invoke(item.id!!)''',
    '''                                                item.id?.let { onEditProduct?.invoke(it) }''',
    "product edit guard",
)
source = replace_once(
    source,
    '''        if (docForActions != null) {
            val doc = docForActions!!''',
    '''        docForActions?.let { doc ->''',
    "document action smart cast",
)
write(path, source)

remaining = []
for kotlin_file in (ROOT / "app/src/main/java").rglob("*.kt"):
    for number, line in enumerate(kotlin_file.read_text(encoding="utf-8").splitlines(), 1):
        if "!!" in line:
            remaining.append(f"{kotlin_file.relative_to(ROOT)}:{number}:{line.strip()}")
if remaining:
    raise RuntimeError("Remaining force unwraps:\n" + "\n".join(remaining))

print("all force unwraps removed")
